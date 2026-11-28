package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.seller.SellerRepositoryPort;
import com.consultafacil.domain.port.out.seller.SellerSaleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSellerCommissionsServiceTest {

    @Mock SellerRepositoryPort sellerRepository;
    @Mock SellerSaleRepositoryPort sellerSaleRepository;
    @Mock SellerSaleMapper sellerSaleMapper;

    @InjectMocks GetSellerCommissionsService service;

    Seller seller;
    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("u-1").name("João Silva").email("joao@email.com")
                .password("x").role(UserRole.PATIENT).build();

        seller = Seller.builder()
                .id("s-1").user(user).slug("JOAO1234")
                .commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_sellerNotFound_throwsResourceNotFoundException() {
        when(sellerRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad-id"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(sellerSaleRepository, never()).findBySellerIdOrderByCreatedAtDesc("bad-id");
    }

    @Test
    void execute_sellerWithNoSales_returnsEmptyList() {
        when(sellerRepository.findById("s-1")).thenReturn(Optional.of(seller));
        when(sellerSaleRepository.findBySellerIdOrderByCreatedAtDesc("s-1")).thenReturn(List.of());

        List<SellerSaleResponseDTO> result = service.execute("s-1");

        assertThat(result).isEmpty();
    }

    @Test
    void execute_sellerWithSales_returnsMappedDTOs() {
        Subscription sub = Subscription.builder().id("sub-1").user(user).planId("monthly").build();
        SellerSale sale1 = SellerSale.builder()
                .id("sale-1").seller(seller).subscription(sub)
                .grossAmount(new BigDecimal("149.90"))
                .commissionAmount(new BigDecimal("29.98"))
                .monthReference(LocalDate.of(2025, 6, 1))
                .status(SellerSaleStatus.PENDING)
                .createdAt(LocalDateTime.of(2025, 6, 10, 12, 0))
                .build();
        SellerSale sale2 = SellerSale.builder()
                .id("sale-2").seller(seller).subscription(sub)
                .grossAmount(new BigDecimal("149.90"))
                .commissionAmount(new BigDecimal("29.98"))
                .monthReference(LocalDate.of(2025, 5, 1))
                .status(SellerSaleStatus.PAID)
                .createdAt(LocalDateTime.of(2025, 5, 15, 9, 0))
                .build();

        SellerSaleResponseDTO dto1 = SellerSaleResponseDTO.builder()
                .id("sale-1").sellerId("s-1").subscriptionId("sub-1")
                .grossAmount(new BigDecimal("149.90")).commissionAmount(new BigDecimal("29.98"))
                .monthReference(LocalDate.of(2025, 6, 1)).status(SellerSaleStatus.PENDING).build();
        SellerSaleResponseDTO dto2 = SellerSaleResponseDTO.builder()
                .id("sale-2").sellerId("s-1").subscriptionId("sub-1")
                .grossAmount(new BigDecimal("149.90")).commissionAmount(new BigDecimal("29.98"))
                .monthReference(LocalDate.of(2025, 5, 1)).status(SellerSaleStatus.PAID).build();

        when(sellerRepository.findById("s-1")).thenReturn(Optional.of(seller));
        when(sellerSaleRepository.findBySellerIdOrderByCreatedAtDesc("s-1")).thenReturn(List.of(sale1, sale2));
        when(sellerSaleMapper.toDTO(sale1)).thenReturn(dto1);
        when(sellerSaleMapper.toDTO(sale2)).thenReturn(dto2);

        List<SellerSaleResponseDTO> result = service.execute("s-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("sale-1");
        assertThat(result.get(1).getId()).isEqualTo("sale-2");
        assertThat(result.get(0).getCommissionAmount()).isEqualByComparingTo("29.98");
    }
}
