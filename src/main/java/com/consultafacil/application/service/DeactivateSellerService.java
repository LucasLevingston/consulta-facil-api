package com.consultafacil.application.service;

import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.application.port.in.DeactivateSellerUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeactivateSellerService implements DeactivateSellerUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final SellerMapper mapper;

    @Override
    @Transactional
    public SellerResponseDTO execute(String sellerId) {
        Seller seller = findSellerById(sellerId);
        seller.setStatus(SellerStatus.INACTIVE);
        seller = sellerRepository.save(seller);
        log.info("[Seller] Deactivated sellerId={}", sellerId);
        return mapper.toDTO(seller);
    }

    private Seller findSellerById(String sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }
}
