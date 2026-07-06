package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.seller.CreateSellerDTO;
import com.consultafacil.api.dto.seller.SellerResponseDTO;

public interface CreateSellerUseCase {
    SellerResponseDTO execute(CreateSellerDTO dto);
}
