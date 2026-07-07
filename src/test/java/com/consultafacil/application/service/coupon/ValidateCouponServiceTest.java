package com.consultafacil.application.service.coupon;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ValidateCouponServiceTest {

    @Mock CouponRepositoryPort couponRepository;
    @Mock CouponUseRepositoryPort couponUseRepository;

    @InjectMocks ValidateCouponService service;

    Coupon activeCoupon;

    @BeforeEach
    void setUp() {
        activeCoupon = Coupon.builder()
                .id("c-1").code("PROMO10").description("10% off")
                .type(CouponType.PERCENT).value(new BigDecimal("10.00"))
                .maxUses(null).currentUses(0).maxUsesPerUser(1)
                .status(CouponStatus.ACTIVE).build();

        when(couponUseRepository.countByUserIdAndCouponId(any(), any())).thenReturn(0L);
    }

    @Test
    void validate_success_returnsDiscountResponse() {
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getCouponId()).isEqualTo("c-1");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("14.99");
        assertThat(result.getFinalPrice()).isEqualByComparingTo("134.91");
    }

    @Test
    void validate_couponNotFound_throwsNotFound() {
        when(couponRepository.findByCodeIgnoreCase("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("INVALID", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validate_couponInactive_throwsBadRequest() {
        activeCoupon.setStatus(CouponStatus.INACTIVE);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        assertThatThrownBy(() -> service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void validate_expired_throwsBadRequest() {
        activeCoupon.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        assertThatThrownBy(() -> service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void validate_maxUsesReached_throwsBadRequest() {
        activeCoupon.setMaxUses(5);
        activeCoupon.setCurrentUses(5);
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        assertThatThrownBy(() -> service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("esgotado");
    }

    @Test
    void validate_userExceededMaxPerUser_throwsBadRequest() {
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));
        when(couponUseRepository.countByUserIdAndCouponId("u-1", "c-1")).thenReturn(1L);

        assertThatThrownBy(() -> service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("máximo");
    }

    @Test
    void validate_wrongPlan_throwsBadRequest() {
        activeCoupon.setApplicablePlanIds("yearly,clinic-yearly");
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        assertThatThrownBy(() -> service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não aplicável");
    }

    @Test
    void validate_applicableToSpecificPlan_success() {
        activeCoupon.setApplicablePlanIds("monthly,yearly");
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getFinalPrice()).isLessThan(new BigDecimal("149.90"));
    }

    @Test
    void validate_percentDiscount_10pct_of149_90() {
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getDiscountAmount()).isEqualByComparingTo("14.99");
    }

    @Test
    void validate_fixedDiscount_30brl_off() {
        activeCoupon.setType(CouponType.FIXED);
        activeCoupon.setValue(new BigDecimal("30.00"));
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getFinalPrice()).isEqualByComparingTo("119.90");
    }

    @Test
    void validate_fixedDiscount_exceedsGross_cappedAtGross() {
        activeCoupon.setType(CouponType.FIXED);
        activeCoupon.setValue(new BigDecimal("200.00"));
        when(couponRepository.findByCodeIgnoreCase("PROMO10")).thenReturn(Optional.of(activeCoupon));

        var result = service.execute("PROMO10", "u-1", "monthly", new BigDecimal("149.90"));

        assertThat(result.getFinalPrice()).isEqualByComparingTo("0.00");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("149.90");
    }
}
