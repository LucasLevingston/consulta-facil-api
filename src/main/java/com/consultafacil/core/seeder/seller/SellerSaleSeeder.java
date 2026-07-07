package com.consultafacil.core.seeder.seller;

import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.repository.seller.SellerRepository;
import com.consultafacil.domain.repository.seller.SellerSaleRepository;
import com.consultafacil.domain.repository.subscription.SubscriptionRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerSaleSeeder {

    private final SellerRepository sellerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SellerSaleRepository sellerSaleRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed() {
        List<Seller> sellers = sellerRepository.findAll();
        if (sellers.isEmpty()) return;

        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if (subscriptions.isEmpty()) return;

        int created = 0;
        for (Subscription sub : subscriptions) {
            if (faker.random().nextInt(100) < 50) continue;
            try {
                if (sellerSaleRepository.findBySubscriptionId(sub.getId()).isPresent()) continue;
                Seller seller = sellers.get(faker.random().nextInt(sellers.size()));
                BigDecimal gross = BigDecimal.valueOf(50 + faker.random().nextInt(200));
                BigDecimal commission = gross.multiply(seller.getCommissionRate())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                SellerSaleStatus status = faker.random().nextInt(100) < 60
                        ? SellerSaleStatus.PAID : SellerSaleStatus.PENDING;
                sellerSaleRepository.save(SellerSale.builder()
                        .seller(seller)
                        .subscription(sub)
                        .grossAmount(gross)
                        .commissionAmount(commission)
                        .monthReference(LocalDate.now().minusMonths(faker.random().nextInt(3)))
                        .status(status)
                        .paidAt(status == SellerSaleStatus.PAID
                                ? LocalDateTime.now().minusDays(faker.random().nextInt(30)) : null)
                        .build());
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar seller sale: {}", e.getMessage());
            }
        }
        log.info("[Seed] SellerSales criadas: {}", created);
    }
}
