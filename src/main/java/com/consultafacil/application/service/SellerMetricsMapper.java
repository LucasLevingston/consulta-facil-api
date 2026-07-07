package com.consultafacil.application.service;

import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerMetricsMapper {

    private final SellerSaleRepositoryPort sellerSaleRepository;
    private final SellerMapper mapper;

    public SellerResponseDTO toDTOWithMetrics(Seller seller) {
        SellerResponseDTO dto = mapper.toDTO(seller);
        dto.setTotalSales(sellerSaleRepository.countBySellerId(seller.getId()));
        dto.setTotalCommission(
                sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PENDING)
                        .add(sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PAID)));
        dto.setPendingCommission(sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PENDING));
        return dto;
    }
}
