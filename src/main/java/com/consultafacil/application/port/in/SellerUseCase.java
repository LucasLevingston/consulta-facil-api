package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.seller.CreateSellerDTO;
import com.consultafacil.api.dto.seller.SellerDashboardDTO;
import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface SellerUseCase {
    SellerResponseDTO createSeller(CreateSellerDTO dto);
    List<SellerResponseDTO> listSellers();
    SellerResponseDTO getSeller(String sellerId);
    SellerResponseDTO updateCommissionRate(String sellerId, BigDecimal commissionRate);
    SellerResponseDTO deactivateSeller(String sellerId);
    SellerResponseDTO activateSeller(String sellerId);
    List<SellerSaleResponseDTO> getCommissions(String sellerId);
    SellerSaleResponseDTO updateCommissionStatus(String saleId, com.consultafacil.domain.enums.SellerSaleStatus status);
    SellerDashboardDTO getMyDashboard(String userId);
}
