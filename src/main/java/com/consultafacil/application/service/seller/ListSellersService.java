package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.application.port.in.ListSellersUseCase;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSellersService implements ListSellersUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final SellerMetricsMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDTO> execute() {
        return sellerRepository.findAll().stream()
                .map(mapper::toDTOWithMetrics)
                .toList();
    }
}
