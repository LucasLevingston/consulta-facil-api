package com.consultafacil.application.service.seller;

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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetSellerDashboardServiceTest {

    @Mock SellerRepositoryPort sellerRepository;
    @Mock SellerSaleRepositoryPort sellerSaleRepository;

    GetSellerDashboardService service;
    Seller seller;

    @BeforeEach
    void setUp() {
        SellerReferralLinkBuilder linkBuilder = new SellerReferralLinkBuilder();
        ReflectionTestUtils.setField(linkBuilder, "appUrl", "http://localhost:3000");
        service = new GetSellerDashboardService(sellerRepository, sellerSaleRepository, new SellerSaleMapper(), linkBuilder);

        User user = User.builder()
                .id("u-1").name("João Silva").email("joao@email.com")
                .password("x").role(UserRole.PATIENT).build();

        seller = Seller.builder()
                .id("s-1").user(user).slug("ABCD1234")
                .commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.ACTIVE)
                .build();

        when(sellerSaleRepository.sumCommissionBySeller(any(), any())).thenReturn(BigDecimal.ZERO);
        when(sellerSaleRepository.countBySellerId(any())).thenReturn(0L);
    }

    @Test
    void execute_success_returnsDashboard() {
        when(sellerRepository.findByUserId("u-1")).thenReturn(Optional.of(seller));
        when(sellerSaleRepository.findBySellerIdOrderByCreatedAtDesc("s-1")).thenReturn(List.of());

        var result = service.execute("u-1");

        assertThat(result.getSlug()).isEqualTo("ABCD1234");
        assertThat(result.getReferralLink()).isEqualTo("http://localhost:3000/ref/ABCD1234");
        assertThat(result.getCommissionRate()).isEqualByComparingTo("20.00");
    }

    @Test
    void execute_notASeller_throwsNotFound() {
        when(sellerRepository.findByUserId("u-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("u-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
