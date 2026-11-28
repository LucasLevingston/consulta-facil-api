package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.coupon.CouponRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateCouponUsageServiceTest {

    @Mock CouponRepositoryPort couponRepository;

    @InjectMocks ValidateCouponUsageService service;

    private Coupon activeCoupon() {
        return Coupon.builder()
                .id("c-1").code("SAVE10").description("10% off")
                .type(CouponType.PERCENT).value(new BigDecimal("10.00"))
                .status(CouponStatus.ACTIVE).currentUses(0)
                .maxUsesPerUser(1)
                .build();
    }

    @Test
    void execute_couponNotFound_throwsResourceNotFoundException() {
        when(couponRepository.findByCodeIgnoreCase("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("GHOST", "u-1", new BigDecimal("100.00")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_inactiveCoupon_returnsInvalidResult() {
        Coupon coupon = activeCoupon();
        coupon.setStatus(CouponStatus.INACTIVE);
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("100.00"));

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("inativo");
    }

    @Test
    void execute_couponNotYetActive_returnsInvalidResult() {
        Coupon coupon = activeCoupon();
        coupon.setStartsAt(LocalDateTime.now().plusDays(1));
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("100.00"));

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("não está ativo");
    }

    @Test
    void execute_expiredCoupon_returnsInvalidResult() {
        Coupon coupon = activeCoupon();
        coupon.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("100.00"));

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("expirado");
    }

    @Test
    void execute_maxUsesReached_returnsInvalidResult() {
        Coupon coupon = activeCoupon();
        coupon.setMaxUses(5);
        coupon.setCurrentUses(5);
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("100.00"));

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("esgotado");
    }

    @Test
    void execute_percentDiscount_calculatesCorrectly() {
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(activeCoupon()));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("149.90"));

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("14.99");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("134.91");
        assertThat(result.getCouponCode()).isEqualTo("SAVE10");
    }

    @Test
    void execute_fixedDiscount_calculatesCorrectly() {
        Coupon coupon = activeCoupon();
        coupon.setType(CouponType.FIXED);
        coupon.setValue(new BigDecimal("30.00"));
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("149.90"));

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("119.90");
    }

    @Test
    void execute_fixedDiscountExceedsAmount_capsAtAmount() {
        Coupon coupon = activeCoupon();
        coupon.setType(CouponType.FIXED);
        coupon.setValue(new BigDecimal("200.00"));
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("149.90"));

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("149.90");
        assertThat(result.getFinalAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void execute_couponWithFutureStartsAt_returnsInvalidResult() {
        Coupon coupon = activeCoupon();
        coupon.setStartsAt(LocalDateTime.now().plusHours(1));
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("100.00"));

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void execute_maxUsesNullAndCurrentUsesHigh_returnsValid() {
        Coupon coupon = activeCoupon();
        coupon.setMaxUses(null);
        coupon.setCurrentUses(9999);
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));

        CouponValidationResultDTO result = service.execute("SAVE10", "u-1", new BigDecimal("100.00"));

        assertThat(result.isValid()).isTrue();
    }
}
