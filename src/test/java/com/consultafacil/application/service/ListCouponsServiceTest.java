package com.consultafacil.application.service;

import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.CouponRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCouponsServiceTest {

    @Mock CouponRepositoryPort couponRepository;

    @Test
    void listCoupons_returnsMappedList() {
        ListCouponsService service = new ListCouponsService(couponRepository, new CouponMapper());
        Coupon activeCoupon = Coupon.builder()
                .id("c-1").code("PROMO10").type(CouponType.PERCENT)
                .value(new BigDecimal("10.00")).maxUsesPerUser(1)
                .status(CouponStatus.ACTIVE).build();
        when(couponRepository.findAll()).thenReturn(List.of(activeCoupon));

        var result = service.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PROMO10");
    }
}
