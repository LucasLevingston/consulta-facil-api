package com.consultafacil.application.port.in.seller;

import com.consultafacil.api.dto.seller.SellerResponseDTO;

import java.util.List;

public interface ListSellersUseCase {
    List<SellerResponseDTO> execute();
}
