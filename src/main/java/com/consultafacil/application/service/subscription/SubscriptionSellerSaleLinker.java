package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import com.consultafacil.domain.port.out.seller.SellerRepositoryPort;
import com.consultafacil.domain.port.out.seller.SellerSaleRepositoryPort;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionSellerSaleLinker {

    private final SellerRepositoryPort sellerRepository;
    private final SellerSaleRepositoryPort sellerSaleRepository;
    private final PlanRepositoryPort planRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;

    public void createSellerSaleIfPresent(Subscription subscription) {
        String slug = subscription.getReferralSlug();
        if (slug == null || slug.isBlank()) return;

        if (sellerSaleRepository.findBySubscriptionId(subscription.getId()).isPresent()) return;

        sellerRepository.findBySlug(slug).ifPresentOrElse(seller -> {
            if (seller.getStatus() != SellerStatus.ACTIVE) {
                log.warn("[Seller] Slug {} found but seller is INACTIVE — skipping sale", slug);
                return;
            }

            Plan plan = planRepository.findBySlug(subscription.getPlanId()).orElse(null);
            BigDecimal grossAmount = plan != null ? plan.getPrice() : BigDecimal.ZERO;
            BigDecimal commissionAmount = grossAmount
                    .multiply(seller.getCommissionRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            SellerSale sale = SellerSale.builder()
                    .seller(seller)
                    .subscription(subscription)
                    .grossAmount(grossAmount)
                    .commissionAmount(commissionAmount)
                    .monthReference(LocalDate.now().withDayOfMonth(1))
                    .status(SellerSaleStatus.PENDING)
                    .build();

            sellerSaleRepository.save(sale);
            subscription.setSellerId(seller.getId());
            subscriptionRepository.save(subscription);

            log.info("[Seller] Sale created for sellerId={} subscriptionId={} commission=R${}",
                    seller.getId(), subscription.getId(), commissionAmount);
        }, () -> log.warn("[Seller] Referral slug '{}' not found — no sale created", slug));
    }
}
