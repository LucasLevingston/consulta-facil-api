package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.CreateSellerDTO;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
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
class CreateSellerServiceTest {

    @Mock SellerRepositoryPort sellerRepository;
    @Mock SellerSaleRepositoryPort sellerSaleRepository;
    @Mock UserRepositoryPort userRepository;

    CreateSellerService service;
    User user;

    @BeforeEach
    void setUp() {
        SellerReferralLinkBuilder linkBuilder = new SellerReferralLinkBuilder();
        ReflectionTestUtils.setField(linkBuilder, "appUrl", "http://localhost:3000");
        SellerMapper mapper = new SellerMapper(linkBuilder);
        service = new CreateSellerService(sellerRepository, userRepository, mapper);

        user = User.builder()
                .id("u-1").name("João Silva").email("joao@email.com")
                .password("x").role(UserRole.PATIENT).build();

        when(sellerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_success_returnsDTOWithReferralLink() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(sellerRepository.existsByUserId("u-1")).thenReturn(false);
        when(sellerRepository.existsBySlug(any())).thenReturn(false);

        var dto = new CreateSellerDTO();
        dto.setUserId("u-1");
        dto.setCommissionRate(new BigDecimal("20.00"));

        var result = service.execute(dto);

        assertThat(result.getReferralLink()).startsWith("http://localhost:3000/ref/");
        assertThat(result.getCommissionRate()).isEqualByComparingTo("20.00");
        verify(sellerRepository).save(any(Seller.class));
    }

    @Test
    void execute_userNotFound_throwsNotFound() {
        when(userRepository.findById("bad")).thenReturn(Optional.empty());

        var dto = new CreateSellerDTO();
        dto.setUserId("bad");
        dto.setCommissionRate(new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.execute(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_duplicateUser_throwsDuplicate() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(sellerRepository.existsByUserId("u-1")).thenReturn(true);

        var dto = new CreateSellerDTO();
        dto.setUserId("u-1");
        dto.setCommissionRate(new BigDecimal("15.00"));

        assertThatThrownBy(() -> service.execute(dto))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
