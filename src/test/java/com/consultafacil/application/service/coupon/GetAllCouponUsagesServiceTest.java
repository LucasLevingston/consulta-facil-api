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
class GetAllCouponUsagesServiceTest {

    @Mock CouponUsageRepositoryPort couponUsageRepository;

    GetAllCouponUsagesService service;
    CouponUsageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CouponUsageMapper();
        service = new GetAllCouponUsagesService(couponUsageRepository, mapper);
    }

    private Coupon coupon(String id, String code) {
        return Coupon.builder()
                .id(id).code(code).type(CouponType.PERCENT)
                .value(new BigDecimal("10.00")).status(CouponStatus.ACTIVE)
                .build();
    }

    private CouponUsage usage(String id, Coupon coupon, String userId) {
        return CouponUsage.builder()
                .id(id).coupon(coupon).userId(userId)
                .paymentId("pay-" + id).discountAmount(new BigDecimal("10.00"))
                .build();
    }

    @Test
    void execute_returnsAllMappedUsages() {
        Coupon c = coupon("c-1", "PROMO10");
        CouponUsage u1 = usage("u1", c, "user-1");
        CouponUsage u2 = usage("u2", c, "user-2");
        when(couponUsageRepository.findAll()).thenReturn(List.of(u1, u2));

        List<CouponUsageResponseDTO> result = service.execute();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CouponUsageResponseDTO::getId)
                .containsExactly("u1", "u2");
        verify(couponUsageRepository).findAll();
    }

    @Test
    void execute_emptyRepository_returnsEmptyList() {
        when(couponUsageRepository.findAll()).thenReturn(List.of());

        List<CouponUsageResponseDTO> result = service.execute();

        assertThat(result).isEmpty();
        verify(couponUsageRepository).findAll();
    }

    @Test
    void execute_mappedDtoContainsCouponFields() {
        Coupon c = coupon("c-2", "SAVE20");
        CouponUsage u = usage("usage-1", c, "user-3");
        when(couponUsageRepository.findAll()).thenReturn(List.of(u));

        List<CouponUsageResponseDTO> result = service.execute();

        assertThat(result).hasSize(1);
        CouponUsageResponseDTO dto = result.get(0);
        assertThat(dto.getCouponId()).isEqualTo("c-2");
        assertThat(dto.getCouponCode()).isEqualTo("SAVE20");
        assertThat(dto.getUserId()).isEqualTo("user-3");
        assertThat(dto.getDiscountAmount()).isEqualByComparingTo("10.00");
    }
}
