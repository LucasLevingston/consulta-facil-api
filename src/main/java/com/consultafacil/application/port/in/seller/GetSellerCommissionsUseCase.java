package com.consultafacil.application.port.in.seller;

import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;

import java.util.List;

public interface GetSellerCommissionsUseCase {
    List<SellerSaleResponseDTO> execute(String sellerId);
}
