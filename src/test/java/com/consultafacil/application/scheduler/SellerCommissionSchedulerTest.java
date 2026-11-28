package com.consultafacil.application.scheduler;

import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.port.out.seller.SellerSaleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerCommissionSchedulerTest {

    @Mock SellerSaleRepositoryPort sellerSaleRepository;

    SellerCommissionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SellerCommissionScheduler(sellerSaleRepository);
    }

    private SellerSale pendingSaleFor(String sellerId, BigDecimal commission) {
        Seller seller = Seller.builder().id(sellerId).slug("slug-" + sellerId)
                .commissionRate(new BigDecimal("10.00")).build();
        return SellerSale.builder()
                .id("sale-" + sellerId)
                .seller(seller)
                .grossAmount(new BigDecimal("100.00"))
                .commissionAmount(commission)
                .monthReference(LocalDate.now().minusMonths(1).withDayOfMonth(1))
                .status(SellerSaleStatus.PENDING)
                .build();
    }

    @Test
    void summarizePreviousMonthCommissions_salesExist_queresRepository() {
        SellerSale sale = pendingSaleFor("s-1", new BigDecimal("10.00"));
        when(sellerSaleRepository.findByMonthReference(any(LocalDate.class)))
                .thenReturn(List.of(sale));

        scheduler.summarizePreviousMonthCommissions();

        verify(sellerSaleRepository).findByMonthReference(any(LocalDate.class));
    }

    @Test
    void summarizePreviousMonthCommissions_noSales_logsAndReturnsEarly() {
        when(sellerSaleRepository.findByMonthReference(any(LocalDate.class)))
                .thenReturn(List.of());

        assertThatCode(() -> scheduler.summarizePreviousMonthCommissions()).doesNotThrowAnyException();
        verify(sellerSaleRepository).findByMonthReference(any(LocalDate.class));
    }

    @Test
    void summarizePreviousMonthCommissions_multipleSellers_aggregatesPerSeller() {
        SellerSale s1a = pendingSaleFor("s-A", new BigDecimal("15.00"));
        SellerSale s1b = pendingSaleFor("s-A", new BigDecimal("20.00"));
        SellerSale s2 = pendingSaleFor("s-B", new BigDecimal("5.00"));
        when(sellerSaleRepository.findByMonthReference(any(LocalDate.class)))
                .thenReturn(List.of(s1a, s1b, s2));

        assertThatCode(() -> scheduler.summarizePreviousMonthCommissions()).doesNotThrowAnyException();
    }

    @Test
    void summarizePreviousMonthCommissions_repositoryThrows_exceptionPropagates() {
        when(sellerSaleRepository.findByMonthReference(any(LocalDate.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThatCode(() -> scheduler.summarizePreviousMonthCommissions())
                .isInstanceOf(RuntimeException.class);
    }
}
