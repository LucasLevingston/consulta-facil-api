package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.application.port.in.ActivateSellerUseCase;
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
public class ActivateSellerService implements ActivateSellerUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final SellerMapper mapper;

    @Override
    @Transactional
    public SellerResponseDTO execute(String sellerId) {
        Seller seller = findSellerById(sellerId);
        seller.setStatus(SellerStatus.ACTIVE);
        seller = sellerRepository.save(seller);
        log.info("[Seller] Activated sellerId={}", sellerId);
        return mapper.toDTO(seller);
    }

    private Seller findSellerById(String sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }
}
