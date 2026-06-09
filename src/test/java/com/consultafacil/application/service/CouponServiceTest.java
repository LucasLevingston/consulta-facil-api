package com.consultafacil.application.service;

import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.api.dto.coupon.UpdateCouponDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import com.consultafacil.domain.port.out.CouponUseRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponServiceTest {

    @Mock CouponRepositoryPort couponRepository;
    @Mock CouponUseRepositoryPort couponUseRepository;

    @InjectMocks CouponService service;

    Coupon activeCoupon;

    @BeforeEach
    void setUp() {
        activeCoupon = Coupon.builder()
                .id("c-1")
                .code("PROMO10")
                .description("10% off")
                .type(CouponType.PERCENT)
                .value(new BigDecimal("10.00"))
                .maxUses(null)
                .currentUses(0)
                .maxUsesPerUser(1)
                .status(CouponStatus.ACTIVE)
                .build();

        when(couponRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(couponUseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(couponUseRepository.countByUserIdAndCouponId(any(), any())).thenReturn(0L);
    }

    // ── createCoupon ──────────────────────────────────────────────────────

    @Test
    void createCoupon_success_savesAndReturnsDTO() {
        CreateCouponDTO dto = new CreateCouponDTO();
        dto.setCode("new10");
        dto.setType(CouponType.PERCENT);
        dto.setValue(new BigDecimal("10.00"));
        dto.setMaxUsesPerUser(1);

        var result = service.createCoupon(dto, "admin-1");

        assertThat(result.getCode()).isEqualTo("NEW10");
        assertThat(result.getType()).isEqualTo(CouponType.PERCENT);
        verify(couponRepository).save(any(Coupon.class));
    }

    // ── updateCoupon ──────────────────────────────────────────────────────

    @Test
    void updateCoupon_notFound_throwsNotFound() {
        when(couponRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCoupon("bad", new UpdateCouponDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCoupon_deactivate_setsStatusInactive() {
        when(couponRepository.findById("c-1")).thenReturn(Optional.of(activeCoupon));

        UpdateCouponDTO dto = new UpdateCouponDTO();
        dto.setStatus(CouponStatus.INACTIVE);

        var result = service.updateCoupon("c-1", dto);

        assertThat(result.getStatus()).isEqualTo(CouponStatus.INACTIVE);
    }

    // ── validate ─────────────────────────────────────────────────────────

    @Test
    void validate_success_returnsDiscountResponse() {
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getCouponId()).isEqualTo("c-1");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("14.99");
        assertThat(result.getFinalPrice()).isEqualByComparingTo("134.91");
    }

    @Test
    void validate_couponNotFound_throwsNotFound() {
        when(couponRepository.findByCodeIgnoreCase("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validate("INVALID", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validate_couponInactive_throwsBadRequest() {
        activeCoupon.setStatus(CouponStatus.INACTIVE);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        assertThatThrownBy(() -> service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void validate_expired_throwsBadRequest() {
        activeCoupon.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        assertThatThrownBy(() -> service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void validate_maxUsesReached_throwsBadRequest() {
        activeCoupon.setMaxUses(5);
        activeCoupon.setCurrentUses(5);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        assertThatThrownBy(() -> service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("esgotado");
    }

    @Test
    void validate_userExceededMaxPerUser_throwsBadRequest() {
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));
        when(couponUseRepository.countByUserIdAndCouponId("u-1", "c-1")).thenReturn(1L);

        assertThatThrownBy(() -> service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("máximo");
    }

    @Test
    void validate_wrongPlan_throwsBadRequest() {
        activeCoupon.setApplicablePlanIds("yearly,clinic-yearly");
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        assertThatThrownBy(() -> service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não aplicável");
    }

    @Test
    void validate_applicableToSpecificPlan_success() {
        activeCoupon.setApplicablePlanIds("monthly,yearly");
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getFinalPrice()).isLessThan(new BigDecimal("149.90"));
    }

    // ── discount calculation ──────────────────────────────────────────────

    @Test
    void validate_percentDiscount_10pct_of149_90() {
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getDiscountAmount()).isEqualByComparingTo("14.99");
    }

    @Test
    void validate_fixedDiscount_30brl_off() {
        activeCoupon.setType(CouponType.FIXED);
        activeCoupon.setValue(new BigDecimal("30.00"));
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getFinalPrice()).isEqualByComparingTo("119.90");
    }

    @Test
    void validate_fixedDiscount_exceedsGross_cappedAtGross() {
        activeCoupon.setType(CouponType.FIXED);
        activeCoupon.setValue(new BigDecimal("200.00"));
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.validate("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getFinalPrice()).isEqualByComparingTo("0.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("149.90");
    }

    // ── listCoupons ───────────────────────────────────────────────────────

    @Test
    void listCoupons_returnsMappedList() {
        when(couponRepository.findAll()).thenReturn(List.of(activeCoupon));

        var result = service.listCoupons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PROMO10");
    }
}
