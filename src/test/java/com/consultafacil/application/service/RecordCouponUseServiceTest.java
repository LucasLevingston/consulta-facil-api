package com.consultafacil.application.service;

import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import com.consultafacil.domain.port.out.CouponUseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordCouponUseServiceTest {

    @Mock CouponRepositoryPort couponRepository;
    @Mock CouponUseRepositoryPort couponUseRepository;

    @InjectMocks RecordCouponUseService service;

    @Test
    void execute_couponFound_incrementsAndSavesUse() {
        Coupon coupon = Coupon.builder().id("c-1").code("PROMO10").build();
        when(couponRepository.findById("c-1")).thenReturn(Optional.of(coupon));
        when(couponRepository.incrementCurrentUses("c-1")).thenReturn(1);

        service.execute("c-1", "u-1", "sub-1", new BigDecimal("10.00"));

        verify(couponUseRepository).save(any());
    }

    @Test
    void execute_couponNotFound_doesNothing() {
        when(couponRepository.findById("bad")).thenReturn(Optional.empty());

        service.execute("bad", "u-1", "sub-1", BigDecimal.TEN);

        verify(couponUseRepository, never()).save(any());
    }
}
