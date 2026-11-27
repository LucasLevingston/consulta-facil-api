package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.api.dto.billing.coupon.CouponValidationResultDTO;
import com.consultafacil.application.port.in.coupon.ValidateCouponUsageUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.entity.CouponUsage;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.coupon.CouponRepositoryPort;
import com.consultafacil.domain.port.out.coupon.CouponUsageRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyCouponServiceTest {

    @Mock CouponRepositoryPort couponRepository;
    @Mock CouponUsageRepositoryPort couponUsageRepository;
    @Mock ValidateCouponUsageUseCase validateCouponUsageUseCase;

    ApplyCouponService service;
    CouponUsageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CouponUsageMapper();
        service = new ApplyCouponService(couponRepository, couponUsageRepository, validateCouponUsageUseCase, mapper);
    }

    private Coupon activeCoupon() {
        return Coupon.builder()
                .id("c-1").code("SAVE10").description("10% off")
                .type(CouponType.PERCENT).value(new BigDecimal("10.00"))
                .status(CouponStatus.ACTIVE).currentUses(0).maxUsesPerUser(1)
                .build();
    }

    @Test
    void execute_validCoupon_returnsCouponUsageResponseDTO() {
        Coupon coupon = activeCoupon();
        CouponValidationResultDTO validation = CouponValidationResultDTO.valid(
                new BigDecimal("14.99"), new BigDecimal("134.91"), "SAVE10");
        when(validateCouponUsageUseCase.execute("SAVE10", "u-1", new BigDecimal("149.90")))
                .thenReturn(validation);
        when(couponRepository.findByCodeIgnoreCase("SAVE10")).thenReturn(Optional.of(coupon));
        CouponUsage saved = CouponUsage.builder()
                .id("usage-1").coupon(coupon).userId("u-1")
                .paymentId("pay-1").discountAmount(new BigDecimal("14.99")).build();
        when(couponUsageRepository.save(any(CouponUsage.class))).thenReturn(saved);

        CouponUsageResponseDTO result = service.execute("SAVE10", "u-1", "pay-1", new BigDecimal("149.90"));

        assertThat(result.getId()).isEqualTo("usage-1");
        assertThat(result.getCouponId()).isEqualTo("c-1");
        assertThat(result.getCouponCode()).isEqualTo("SAVE10");
        assertThat(result.getUserId()).isEqualTo("u-1");
        assertThat(result.getDiscountAmount()).isEqualByComparingTo("14.99");
        verify(couponRepository).incrementCurrentUses("c-1");
        verify(couponUsageRepository).save(any(CouponUsage.class));
    }

    @Test
    void execute_invalidValidationResult_throwsBadRequestException() {
        CouponValidationResultDTO invalid = CouponValidationResultDTO.invalid("Cupom expirado");
        when(validateCouponUsageUseCase.execute(anyString(), anyString(), any(BigDecimal.class)))
                .thenReturn(invalid);

        assertThatThrownBy(() -> service.execute("BAD", "u-1", "pay-1", new BigDecimal("100.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cupom inválido");

        verify(couponRepository, never()).findByCodeIgnoreCase(anyString());
        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void execute_couponNotFoundAfterValidation_throwsResourceNotFoundException() {
        CouponValidationResultDTO validation = CouponValidationResultDTO.valid(
                new BigDecimal("10.00"), new BigDecimal("90.00"), "GHOST");
        when(validateCouponUsageUseCase.execute("GHOST", "u-1", new BigDecimal("100.00")))
                .thenReturn(validation);
        when(couponRepository.findByCodeIgnoreCase("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("GHOST", "u-1", "pay-1", new BigDecimal("100.00")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(couponUsageRepository, never()).save(any());
    }
}
