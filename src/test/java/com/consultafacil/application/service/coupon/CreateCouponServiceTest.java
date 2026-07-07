package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.coupon.CreateCouponDTO;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.coupon.CouponRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCouponServiceTest {

    @Mock CouponRepositoryPort couponRepository;

    CreateCouponService service;

    @BeforeEach
    void setUp() {
        service = new CreateCouponService(couponRepository, new CouponMapper());
        when(couponRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createCoupon_success_savesAndReturnsDTO() {
        CreateCouponDTO dto = new CreateCouponDTO();
        dto.setCode("new10");
        dto.setType(CouponType.PERCENT);
        dto.setValue(new BigDecimal("10.00"));
        dto.setMaxUsesPerUser(1);

        var result = service.execute(dto, "admin-1");

        assertThat(result.getCode()).isEqualTo("NEW10");
        assertThat(result.getType()).isEqualTo(CouponType.PERCENT);
        verify(couponRepository).save(any());
    }
}
