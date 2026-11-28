package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.seller.SellerRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSellersServiceTest {

    @Mock SellerRepositoryPort sellerRepository;
    @Mock SellerMetricsMapper sellerMetricsMapper;

    @InjectMocks ListSellersService service;

    User user1;
    User user2;
    Seller seller1;
    Seller seller2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id("u-1").name("João Silva").email("joao@email.com")
                .password("x").role(UserRole.PATIENT).build();
        user2 = User.builder()
                .id("u-2").name("Maria Santos").email("maria@email.com")
                .password("y").role(UserRole.PATIENT).build();

        seller1 = Seller.builder()
                .id("s-1").user(user1).slug("JOAO1234")
                .commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.ACTIVE).build();
        seller2 = Seller.builder()
                .id("s-2").user(user2).slug("MARI5678")
                .commissionRate(new BigDecimal("15.00"))
                .status(SellerStatus.ACTIVE).build();
    }

    @Test
    void execute_noSellers_returnsEmptyList() {
        when(sellerRepository.findAll()).thenReturn(List.of());

        List<SellerResponseDTO> result = service.execute();

        assertThat(result).isEmpty();
        verify(sellerMetricsMapper, never()).toDTOWithMetrics(any());
    }

    @Test
    void execute_multipleSellers_returnsMappedDTOs() {
        SellerResponseDTO dto1 = SellerResponseDTO.builder()
                .id("s-1").userId("u-1").userName("João Silva")
                .slug("JOAO1234").commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.ACTIVE).totalSales(5L)
                .totalCommission(new BigDecimal("100.00"))
                .pendingCommission(new BigDecimal("50.00")).build();
        SellerResponseDTO dto2 = SellerResponseDTO.builder()
                .id("s-2").userId("u-2").userName("Maria Santos")
                .slug("MARI5678").commissionRate(new BigDecimal("15.00"))
                .status(SellerStatus.ACTIVE).totalSales(3L)
                .totalCommission(new BigDecimal("60.00"))
                .pendingCommission(new BigDecimal("60.00")).build();

        when(sellerRepository.findAll()).thenReturn(List.of(seller1, seller2));
        when(sellerMetricsMapper.toDTOWithMetrics(seller1)).thenReturn(dto1);
        when(sellerMetricsMapper.toDTOWithMetrics(seller2)).thenReturn(dto2);

        List<SellerResponseDTO> result = service.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("s-1");
        assertThat(result.get(1).getId()).isEqualTo("s-2");
        assertThat(result.get(0).getTotalSales()).isEqualTo(5L);
        assertThat(result.get(1).getCommissionRate()).isEqualByComparingTo("15.00");
    }

    @Test
    void execute_singleSeller_returnsSingleElementList() {
        SellerResponseDTO dto = SellerResponseDTO.builder()
                .id("s-1").userId("u-1").userName("João Silva")
                .slug("JOAO1234").commissionRate(new BigDecimal("20.00"))
                .status(SellerStatus.ACTIVE).totalSales(0L)
                .totalCommission(BigDecimal.ZERO)
                .pendingCommission(BigDecimal.ZERO).build();

        when(sellerRepository.findAll()).thenReturn(List.of(seller1));
        when(sellerMetricsMapper.toDTOWithMetrics(seller1)).thenReturn(dto);

        List<SellerResponseDTO> result = service.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("JOAO1234");
    }
}
