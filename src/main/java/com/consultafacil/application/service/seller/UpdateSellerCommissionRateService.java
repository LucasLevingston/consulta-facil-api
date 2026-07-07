package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.application.port.in.UpdateSellerCommissionRateUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSellerCommissionRateService implements UpdateSellerCommissionRateUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final SellerMetricsMapper mapper;

    @Override
    @Transactional
    public SellerResponseDTO execute(String sellerId, BigDecimal commissionRate) {
        Seller seller = findSellerById(sellerId);
        seller.setCommissionRate(commissionRate);
        seller = sellerRepository.save(seller);
        log.info("[Seller] Updated commission rate for sellerId={} to {}%", sellerId, commissionRate);
        return mapper.toDTOWithMetrics(seller);
    }

    private Seller findSellerById(String sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }
}
