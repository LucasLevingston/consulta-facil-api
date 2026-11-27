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
class GetUserCouponHistoryServiceTest {

    @Mock CouponUsageRepositoryPort couponUsageRepository;

    GetUserCouponHistoryService service;
    CouponUsageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CouponUsageMapper();
        service = new GetUserCouponHistoryService(couponUsageRepository, mapper);
    }

    private Coupon coupon(String id, String code) {
        return Coupon.builder()
                .id(id).code(code).type(CouponType.PERCENT)
                .value(new BigDecimal("15.00")).status(CouponStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_returnsUsageHistoryForGivenUser() {
        Coupon c1 = coupon("c-10", "TEN");
        Coupon c2 = coupon("c-20", "TWENTY");
        CouponUsage u1 = CouponUsage.builder()
                .id("h-1").coupon(c1).userId("user-X")
                .paymentId("px-1").discountAmount(new BigDecimal("15.00")).build();
        CouponUsage u2 = CouponUsage.builder()
                .id("h-2").coupon(c2).userId("user-X")
                .paymentId("px-2").discountAmount(new BigDecimal("30.00")).build();
        when(couponUsageRepository.findByUserId("user-X")).thenReturn(List.of(u1, u2));

        List<CouponUsageResponseDTO> result = service.execute("user-X");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CouponUsageResponseDTO::getUserId)
                .containsOnly("user-X");
        verify(couponUsageRepository).findByUserId("user-X");
    }

    @Test
    void execute_userWithNoHistory_returnsEmptyList() {
        when(couponUsageRepository.findByUserId("user-new")).thenReturn(List.of());

        List<CouponUsageResponseDTO> result = service.execute("user-new");

        assertThat(result).isEmpty();
        verify(couponUsageRepository).findByUserId("user-new");
    }

    @Test
    void execute_singleEntry_mappedCorrectly() {
        Coupon c = coupon("c-30", "THIRTY");
        CouponUsage u = CouponUsage.builder()
                .id("h-9").coupon(c).userId("user-Y")
                .paymentId("py-9").discountAmount(new BigDecimal("30.00")).build();
        when(couponUsageRepository.findByUserId("user-Y")).thenReturn(List.of(u));

        List<CouponUsageResponseDTO> result = service.execute("user-Y");

        assertThat(result).hasSize(1);
        CouponUsageResponseDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo("h-9");
        assertThat(dto.getCouponCode()).isEqualTo("THIRTY");
        assertThat(dto.getPaymentId()).isEqualTo("py-9");
        assertThat(dto.getDiscountAmount()).isEqualByComparingTo("30.00");
    }
}
