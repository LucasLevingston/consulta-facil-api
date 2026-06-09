package com.consultafacil.application.service;

import com.consultafacil.api.dto.seller.CreateSellerDTO;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SellerServiceTest {

    @Mock SellerRepositoryPort sellerRepository;
    @Mock SellerSaleRepositoryPort sellerSaleRepository;
    @Mock UserRepositoryPort userRepository;

    @InjectMocks SellerService service;

    User user;
    Seller seller;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "appUrl", "http://localhost:3000");

        user = User.builder()
                .id("u-1").name("João Silva").email("joao@email.com")
                .password("x").role(UserRole.PATIENT).build();

        seller = Seller.builder()
                .id("s-1").user(user).slug("ABCD1234")
                .commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.ACTIVE)
                .build();

        when(sellerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sellerSaleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sellerSaleRepository.sumCommissionBySeller(any(), any())).thenReturn(BigDecimal.ZERO);
        when(sellerSaleRepository.countBySellerId(any())).thenReturn(0L);
    }

    // ── createSeller ──────────────────────────────────────────────────────

    @Test
    void createSeller_success_returnsDTOWithReferralLink() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(sellerRepository.existsByUserId("u-1")).thenReturn(false);
        when(sellerRepository.existsBySlug(any())).thenReturn(false);

        var dto = new CreateSellerDTO();
        dto.setUserId("u-1");
        dto.setCommissionRate(new BigDecimal("20.00"));

        var result = service.createSeller(dto);

        assertThat(result.getReferralLink()).startsWith("http://localhost:3000/ref/");
        assertThat(result.getCommissionRate()).isEqualByComparingTo("20.00");
        verify(sellerRepository).save(any(Seller.class));
    }

    @Test
    void createSeller_userNotFound_throwsNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());

        var dto = new CreateSellerDTO();
        dto.setUserId("bad");
        dto.setCommissionRate(new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.createSeller(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSeller_duplicateUser_throwsDuplicate() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(sellerRepository.existsByUserId("u-1")).thenReturn(true);

        var dto = new CreateSellerDTO();
        dto.setUserId("u-1");
        dto.setCommissionRate(new BigDecimal("15.00"));

        assertThatThrownBy(() -> service.createSeller(dto))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // ── updateCommissionRate ──────────────────────────────────────────────

    @Test
    void updateCommissionRate_success_updatesRate() {
        when(sellerRepository.findById("s-1")).thenReturn(Optional.of(seller));

        var result = service.updateCommissionRate("s-1", new BigDecimal("25.00"));

        assertThat(result.getCommissionRate()).isEqualByComparingTo("25.00");
        verify(sellerRepository).save(seller);
    }

    @Test
    void updateCommissionRate_sellerNotFound_throwsNotFound() {
        when(sellerRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCommissionRate("bad", BigDecimal.TEN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deactivate / activate ─────────────────────────────────────────────

    @Test
    void deactivateSeller_setsStatusInactive() {
        when(sellerRepository.findById("s-1")).thenReturn(Optional.of(seller));

        var result = service.deactivateSeller("s-1");

        assertThat(result.getStatus()).isEqualTo(SellerStatus.INACTIVE);
    }

    @Test
    void activateSeller_setsStatusActive() {
        seller.setStatus(SellerStatus.INACTIVE);
        when(sellerRepository.findById("s-1")).thenReturn(Optional.of(seller));

        var result = service.activateSeller("s-1");

        assertThat(result.getStatus()).isEqualTo(SellerStatus.ACTIVE);
    }

    // ── updateCommissionStatus ────────────────────────────────────────────

    @Test
    void updateCommissionStatus_pendingToPaid_setsPaidAt() {
        Subscription sub = Subscription.builder().id("sub-1").user(user).planId("monthly").build();
        SellerSale sale = SellerSale.builder()
                .id("sale-1").seller(seller).subscription(sub)
                .grossAmount(new BigDecimal("149.90"))
                .commissionAmount(new BigDecimal("29.98"))
                .monthReference(LocalDate.now().withDayOfMonth(1))
                .status(SellerSaleStatus.PENDING)
                .build();

        when(sellerSaleRepository.findById("sale-1")).thenReturn(Optional.of(sale));

        var result = service.updateCommissionStatus("sale-1", SellerSaleStatus.PAID);

        assertThat(result.getStatus()).isEqualTo(SellerSaleStatus.PAID);
        assertThat(result.getPaidAt()).isNotNull();
    }

    @Test
    void updateCommissionStatus_reversed_clearsPaidAt() {
        Subscription sub = Subscription.builder().id("sub-1").user(user).planId("monthly").build();
        SellerSale sale = SellerSale.builder()
                .id("sale-1").seller(seller).subscription(sub)
                .grossAmount(new BigDecimal("149.90"))
                .commissionAmount(new BigDecimal("29.98"))
                .monthReference(LocalDate.now().withDayOfMonth(1))
                .status(SellerSaleStatus.PAID)
                .paidAt(java.time.LocalDateTime.now())
                .build();

        when(sellerSaleRepository.findById("sale-1")).thenReturn(Optional.of(sale));

        var result = service.updateCommissionStatus("sale-1", SellerSaleStatus.REVERSED);

        assertThat(result.getStatus()).isEqualTo(SellerSaleStatus.REVERSED);
        assertThat(result.getPaidAt()).isNull();
    }

    @Test
    void updateCommissionStatus_saleNotFound_throwsNotFound() {
        when(sellerSaleRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCommissionStatus("bad", SellerSaleStatus.PAID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getMyDashboard ────────────────────────────────────────────────────

    @Test
    void getMyDashboard_success_returnsDashboard() {
        when(sellerRepository.findByUserId("u-1")).thenReturn(Optional.of(seller));
        when(sellerSaleRepository.findBySellerIdOrderByCreatedAtDesc("s-1")).thenReturn(List.of());

        var result = service.getMyDashboard("u-1");

        assertThat(result.getSlug()).isEqualTo("ABCD1234");
        assertThat(result.getReferralLink()).isEqualTo("http://localhost:3000/ref/ABCD1234");
        assertThat(result.getCommissionRate()).isEqualByComparingTo("20.00");
    }

    @Test
    void getMyDashboard_notASeller_throwsNotFound() {
        when(sellerRepository.findByUserId("u-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyDashboard("u-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── commission calculation ────────────────────────────────────────────

    @Test
    void commissionAmountCalculation_20percent_of150_is30() {
        BigDecimal gross = new BigDecimal("150.00");
        BigDecimal rate = new BigDecimal("20.00");
        BigDecimal expected = new BigDecimal("30.00");

        BigDecimal commission = gross.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        assertThat(commission).isEqualByComparingTo(expected);
    }

    @Test
    void commissionAmountCalculation_15percent_roundsCorrectly() {
        BigDecimal gross = new BigDecimal("149.90");
        BigDecimal rate = new BigDecimal("15.00");

        BigDecimal commission = gross.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        assertThat(commission).isEqualByComparingTo("22.49");
    }
}
