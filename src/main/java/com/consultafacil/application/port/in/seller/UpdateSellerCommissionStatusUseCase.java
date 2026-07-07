package com.consultafacil.application.port.in.seller;

import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;
import com.consultafacil.domain.enums.SellerSaleStatus;

public interface UpdateSellerCommissionStatusUseCase {
    SellerSaleResponseDTO execute(String saleId, SellerSaleStatus status);
}
