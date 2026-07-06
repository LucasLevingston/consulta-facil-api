package com.consultafacil.application.service;

import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;
import com.consultafacil.application.port.in.UpdateSellerCommissionStatusUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSellerCommissionStatusService implements UpdateSellerCommissionStatusUseCase {

    private final SellerSaleRepositoryPort sellerSaleRepository;
    private final SellerSaleMapper mapper;

    @Override
    @Transactional
    public SellerSaleResponseDTO execute(String saleId, SellerSaleStatus status) {
        SellerSale sale = sellerSaleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerSale", saleId));

        sale.setStatus(status);
        if (status == SellerSaleStatus.PAID) {
            sale.setPaidAt(LocalDateTime.now());
        } else if (status == SellerSaleStatus.REVERSED) {
            sale.setPaidAt(null);
        }

        sale = sellerSaleRepository.save(sale);
        log.info("[Seller] Sale {} status updated to {} for sellerId={}", saleId, status, sale.getSeller().getId());
        return mapper.toDTO(sale);
    }
}
