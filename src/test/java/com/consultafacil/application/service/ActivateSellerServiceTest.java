package com.consultafacil.application.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivateSellerServiceTest {

    @Mock SellerRepositoryPort sellerRepository;
    @Mock SellerSaleRepositoryPort sellerSaleRepository;

    ActivateSellerService service;
    Seller seller;

    @BeforeEach
    void setUp() {
        SellerReferralLinkBuilder linkBuilder = new SellerReferralLinkBuilder();
        ReflectionTestUtils.setField(linkBuilder, "appUrl", "http://localhost:3000");
        SellerMapper mapper = new SellerMapper(linkBuilder);
        service = new ActivateSellerService(sellerRepository, mapper);

        User user = User.builder()
                .id("u-1").name("João Silva").email("joao@email.com")
                .password("x").role(UserRole.PATIENT).build();

        seller = Seller.builder()
                .id("s-1").user(user).slug("ABCD1234")
                .commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.INACTIVE)
                .build();

        when(sellerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_setsStatusActive() {
        when(sellerRepository.findById("s-1")).thenReturn(Optional.of(seller));

        var result = service.execute("s-1");

        assertThat(result.getStatus()).isEqualTo(SellerStatus.ACTIVE);
    }
}
