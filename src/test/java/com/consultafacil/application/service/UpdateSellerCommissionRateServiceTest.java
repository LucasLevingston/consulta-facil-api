package com.consultafacil.application.service;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateSellerCommissionRateServiceTest {

    @Mock SellerRepositoryPort sellerRepository;
    @Mock SellerSaleRepositoryPort sellerSaleRepository;

    UpdateSellerCommissionRateService service;
    Seller seller;

    @BeforeEach
    void setUp() {
        SellerReferralLinkBuilder linkBuilder = new SellerReferralLinkBuilder();
        ReflectionTestUtils.setField(linkBuilder, "appUrl", "http://localhost:3000");
        SellerMapper mapper = new SellerMapper(linkBuilder);
        SellerMetricsMapper metricsMapper = new SellerMetricsMapper(sellerSaleRepository, mapper);
        service = new UpdateSellerCommissionRateService(sellerRepository, metricsMapper);

        User user = User.builder()
                .id("u-1").name("João Silva").email("joao@email.com")
                .password("x").role(UserRole.PATIENT).build();

        seller = Seller.builder()
                .id("s-1").user(user).slug("ABCD1234")
                .commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.ACTIVE)
                .build();

        when(sellerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sellerSaleRepository.sumCommissionBySeller(any(), any())).thenReturn(BigDecimal.ZERO);
        when(sellerSaleRepository.countBySellerId(any())).thenReturn(0L);
    }

    @Test
    void execute_success_updatesRate() {
        when(sellerRepository.findById("s-1")).thenReturn(Optional.of(seller));

        var result = service.execute("s-1", new BigDecimal("25.00"));

        assertThat(result.getCommissionRate()).isEqualByComparingTo("25.00");
        verify(sellerRepository).save(seller);
    }

    @Test
    void execute_sellerNotFound_throwsNotFound() {
        when(sellerRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad", BigDecimal.TEN))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
