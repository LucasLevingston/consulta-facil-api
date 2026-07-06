package com.consultafacil.application.service;

import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.application.port.in.GetSellerUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSellerService implements GetSellerUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final SellerMetricsMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SellerResponseDTO execute(String sellerId) {
        Seller seller = findSellerById(sellerId);
        return mapper.toDTOWithMetrics(seller);
    }

    private Seller findSellerById(String sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }
}
