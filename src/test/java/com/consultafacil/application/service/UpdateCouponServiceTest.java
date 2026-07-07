package com.consultafacil.application.service;

import com.consultafacil.api.dto.coupon.UpdateCouponDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateCouponServiceTest {

    @Mock CouponRepositoryPort couponRepository;

    UpdateCouponService service;
    Coupon activeCoupon;

    @BeforeEach
    void setUp() {
        service = new UpdateCouponService(couponRepository, new CouponMapper());
        activeCoupon = Coupon.builder()
                .id("c-1").code("PROMO10").type(CouponType.PERCENT)
                .value(new BigDecimal("10.00")).maxUsesPerUser(1)
                .status(CouponStatus.ACTIVE).build();
        when(couponRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void updateCoupon_notFound_throwsNotFound() {
        when(couponRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad", new UpdateCouponDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCoupon_deactivate_setsStatusInactive() {
        when(couponRepository.findById("c-1")).thenReturn(Optional.of(activeCoupon));

        UpdateCouponDTO dto = new UpdateCouponDTO();
        dto.setStatus(CouponStatus.INACTIVE);

        var result = service.execute("c-1", dto);

        assertThat(result.getStatus()).isEqualTo(CouponStatus.INACTIVE);
    }
}
