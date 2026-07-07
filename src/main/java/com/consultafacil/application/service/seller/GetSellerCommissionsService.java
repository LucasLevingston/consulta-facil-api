package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;
import com.consultafacil.application.port.in.GetSellerCommissionsUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSellerCommissionsService implements GetSellerCommissionsUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final SellerSaleRepositoryPort sellerSaleRepository;
    private final SellerSaleMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SellerSaleResponseDTO> execute(String sellerId) {
        findSellerById(sellerId);
        return sellerSaleRepository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream()
                .map(mapper::toDTO)
                .toList();
    }

    private void findSellerById(String sellerId) {
        sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }
}
