package com.consultafacil.application.service;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateSellerCommissionStatusServiceTest {

    @Mock SellerSaleRepositoryPort sellerSaleRepository;

    UpdateSellerCommissionStatusService service;
    Seller seller;
    User user;

    @BeforeEach
    void setUp() {
        service = new UpdateSellerCommissionStatusService(sellerSaleRepository, new SellerSaleMapper());

        user = User.builder()
                .id("u-1").name("João Silva").email("joao@email.com")
                .password("x").role(UserRole.PATIENT).build();

        seller = Seller.builder()
                .id("s-1").user(user).slug("ABCD1234")
                .commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.ACTIVE)
                .build();

        when(sellerSaleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_pendingToPaid_setsPaidAt() {
        SellerSale sale = buildSale(SellerSaleStatus.PENDING, null);
        when(sellerSaleRepository.findById("sale-1")).thenReturn(Optional.of(sale));

        var result = service.execute("sale-1", SellerSaleStatus.PAID);

        assertThat(result.getStatus()).isEqualTo(SellerSaleStatus.PAID);
        assertThat(result.getPaidAt()).isNotNull();
    }

    @Test
    void execute_reversed_clearsPaidAt() {
        SellerSale sale = buildSale(SellerSaleStatus.PAID, LocalDateTime.now());
        when(sellerSaleRepository.findById("sale-1")).thenReturn(Optional.of(sale));

        var result = service.execute("sale-1", SellerSaleStatus.REVERSED);

        assertThat(result.getStatus()).isEqualTo(SellerSaleStatus.REVERSED);
        assertThat(result.getPaidAt()).isNull();
    }

    @Test
    void execute_saleNotFound_throwsNotFound() {
        when(sellerSaleRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad", SellerSaleStatus.PAID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private SellerSale buildSale(SellerSaleStatus status, LocalDateTime paidAt) {
        Subscription sub = Subscription.builder().id("sub-1").user(user).planId("monthly").build();
        return SellerSale.builder()
                .id("sale-1").seller(seller).subscription(sub)
                .grossAmount(new BigDecimal("149.90"))
                .commissionAmount(new BigDecimal("29.98"))
                .monthReference(LocalDate.now().withDayOfMonth(1))
                .status(status)
                .paidAt(paidAt)
                .build();
    }
}
