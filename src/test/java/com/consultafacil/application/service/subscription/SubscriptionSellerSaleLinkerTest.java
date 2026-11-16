package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import com.consultafacil.domain.port.out.seller.SellerRepositoryPort;
import com.consultafacil.domain.port.out.seller.SellerSaleRepositoryPort;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionSellerSaleLinkerTest {

    @Mock SellerRepositoryPort sellerRepository;
    @Mock SellerSaleRepositoryPort sellerSaleRepository;
    @Mock PlanRepositoryPort planRepository;
    @Mock SubscriptionRepositoryPort subscriptionRepository;

    SubscriptionSellerSaleLinker linker;

    @BeforeEach
    void setUp() {
        linker = new SubscriptionSellerSaleLinker(sellerRepository, sellerSaleRepository, planRepository, subscriptionRepository);
    }

    private Subscription subscription(String slug) {
        return Subscription.builder().id("sub-1").planId("monthly").referralSlug(slug).build();
    }

    private Seller seller(SellerStatus status) {
        return Seller.builder().id("seller-1").slug("joao").commissionRate(new BigDecimal("15")).status(status).build();
    }

    @Test
    void createsPendingSaleWithCommissionOverPlanPriceAndLinksSeller() {
        Subscription subscription = subscription("joao");
        when(sellerSaleRepository.findBySubscriptionId("sub-1")).thenReturn(Optional.empty());
        when(sellerRepository.findBySlug("joao")).thenReturn(Optional.of(seller(SellerStatus.ACTIVE)));
        when(planRepository.findBySlug("monthly"))
                .thenReturn(Optional.of(Plan.builder().slug("monthly").price(new BigDecimal("79.90")).build()));

        linker.createSellerSaleIfPresent(subscription);

        ArgumentCaptor<SellerSale> sale = ArgumentCaptor.forClass(SellerSale.class);
        verify(sellerSaleRepository).save(sale.capture());
        assertThat(sale.getValue().getGrossAmount()).isEqualByComparingTo("79.90");
        assertThat(sale.getValue().getCommissionAmount()).isEqualByComparingTo("11.99");
        assertThat(sale.getValue().getStatus()).isEqualTo(SellerSaleStatus.PENDING);
        assertThat(sale.getValue().getMonthReference()).isEqualTo(LocalDate.now().withDayOfMonth(1));
        assertThat(sale.getValue().getSubscription()).isSameAs(subscription);
        assertThat(subscription.getSellerId()).isEqualTo("seller-1");
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void unknownPlanYieldsZeroValueSale() {
        when(sellerSaleRepository.findBySubscriptionId("sub-1")).thenReturn(Optional.empty());
        when(sellerRepository.findBySlug("joao")).thenReturn(Optional.of(seller(SellerStatus.ACTIVE)));
        when(planRepository.findBySlug("monthly")).thenReturn(Optional.empty());

        linker.createSellerSaleIfPresent(subscription("joao"));

        ArgumentCaptor<SellerSale> sale = ArgumentCaptor.forClass(SellerSale.class);
        verify(sellerSaleRepository).save(sale.capture());
        assertThat(sale.getValue().getGrossAmount()).isEqualByComparingTo("0");
        assertThat(sale.getValue().getCommissionAmount()).isEqualByComparingTo("0");
    }

    @Test
    void ignoresSubscriptionsWithoutReferralSlug() {
        linker.createSellerSaleIfPresent(subscription(null));
        linker.createSellerSaleIfPresent(subscription("  "));

        verifyNoInteractions(sellerRepository, sellerSaleRepository, planRepository, subscriptionRepository);
    }

    @Test
    void doesNotCreateASecondSaleForTheSameSubscription() {
        when(sellerSaleRepository.findBySubscriptionId("sub-1")).thenReturn(Optional.of(SellerSale.builder().id("s").build()));

        linker.createSellerSaleIfPresent(subscription("joao"));

        verify(sellerSaleRepository, never()).save(any());
        verifyNoInteractions(sellerRepository);
    }

    @Test
    void inactiveSellerGetsNoSale() {
        when(sellerSaleRepository.findBySubscriptionId("sub-1")).thenReturn(Optional.empty());
        when(sellerRepository.findBySlug("joao")).thenReturn(Optional.of(seller(SellerStatus.INACTIVE)));

        linker.createSellerSaleIfPresent(subscription("joao"));

        verify(sellerSaleRepository, never()).save(any());
        verifyNoInteractions(planRepository, subscriptionRepository);
    }

    @Test
    void unknownSellerSlugGetsNoSale() {
        when(sellerSaleRepository.findBySubscriptionId("sub-1")).thenReturn(Optional.empty());
        when(sellerRepository.findBySlug("ghost")).thenReturn(Optional.empty());

        linker.createSellerSaleIfPresent(subscription("ghost"));

        verify(sellerSaleRepository, never()).save(any());
        verifyNoInteractions(planRepository, subscriptionRepository);
    }
}
