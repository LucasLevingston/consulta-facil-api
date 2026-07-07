package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;
import com.consultafacil.domain.entity.SellerSale;
import org.springframework.stereotype.Component;

@Component
public class SellerSaleMapper {

    public SellerSaleResponseDTO toDTO(SellerSale sale) {
        return SellerSaleResponseDTO.builder()
                .id(sale.getId())
                .sellerId(sale.getSeller().getId())
                .subscriptionId(sale.getSubscription().getId())
                .grossAmount(sale.getGrossAmount())
                .commissionAmount(sale.getCommissionAmount())
                .monthReference(sale.getMonthReference())
                .status(sale.getStatus())
                .paidAt(sale.getPaidAt())
                .createdAt(sale.getCreatedAt())
                .build();
    }
}
