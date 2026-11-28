package com.consultafacil.application.service.coupon;

import com.consultafacil.api.dto.billing.coupon.CouponUsageResponseDTO;
import com.consultafacil.domain.entity.Coupon;
import com.consultafacil.domain.entity.CouponUsage;
import com.consultafacil.domain.enums.CouponStatus;
import com.consultafacil.domain.enums.CouponType;
import com.consultafacil.domain.port.out.coupon.CouponUsageRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCouponUsagesByCouponIdServiceTest {

    @Mock CouponUsageRepositoryPort couponUsageRepository;

    GetCouponUsagesByCouponIdService service;
    CouponUsageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CouponUsageMapper();
        service = new GetCouponUsagesByCouponIdService(couponUsageRepository, mapper);
    }

    private Coupon coupon(String id, String code) {
        return Coupon.builder()
                .id(id).code(code).type(CouponType.FIXED)
                .value(new BigDecimal("20.00")).status(CouponStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_returnsUsagesForGivenCouponId() {
        Coupon c = coupon("coupon-99", "FLAT20");
        CouponUsage u1 = CouponUsage.builder()
                .id("usage-a").coupon(c).userId("user-A")
                .paymentId("p-A").discountAmount(new BigDecimal("20.00")).build();
        CouponUsage u2 = CouponUsage.builder()
                .id("usage-b").coupon(c).userId("user-B")
                .paymentId("p-B").discountAmount(new BigDecimal("20.00")).build();
        when(couponUsageRepository.findByCouponId("coupon-99")).thenReturn(List.of(u1, u2));

        List<CouponUsageResponseDTO> result = service.execute("coupon-99");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CouponUsageResponseDTO::getCouponId)
                .containsOnly("coupon-99");
        verify(couponUsageRepository).findByCouponId("coupon-99");
    }

    @Test
    void execute_noUsagesForCoupon_returnsEmptyList() {
        when(couponUsageRepository.findByCouponId("coupon-00")).thenReturn(List.of());

        List<CouponUsageResponseDTO> result = service.execute("coupon-00");

        assertThat(result).isEmpty();
        verify(couponUsageRepository).findByCouponId("coupon-00");
    }

    @Test
    void execute_mappedDtoContainsCorrectFields() {
        Coupon c = coupon("c-5", "CODE5");
        CouponUsage u = CouponUsage.builder()
                .id("uid-5").coupon(c).userId("usr-5")
                .paymentId("pmnt-5").discountAmount(new BigDecimal("20.00")).build();
        when(couponUsageRepository.findByCouponId("c-5")).thenReturn(List.of(u));

        List<CouponUsageResponseDTO> result = service.execute("c-5");

        CouponUsageResponseDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo("uid-5");
        assertThat(dto.getCouponCode()).isEqualTo("CODE5");
        assertThat(dto.getUserId()).isEqualTo("usr-5");
        assertThat(dto.getPaymentId()).isEqualTo("pmnt-5");
    }
}
