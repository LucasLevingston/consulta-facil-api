package com.consultafacil.application.service.seller;

import com.consultafacil.api.dto.seller.SellerDashboardDTO;
import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;
import com.consultafacil.application.port.in.GetSellerDashboardUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSellerDashboardService implements GetSellerDashboardUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final SellerSaleRepositoryPort sellerSaleRepository;
    private final SellerSaleMapper saleMapper;
    private final SellerReferralLinkBuilder referralLinkBuilder;

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardDTO execute(String userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller com userId", userId));

        List<SellerSaleResponseDTO> recentSales = sellerSaleRepository
                .findBySellerIdOrderByCreatedAtDesc(seller.getId()).stream()
                .limit(20)
                .map(saleMapper::toDTO)
                .toList();

        BigDecimal totalCommission = sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PENDING)
                .add(sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PAID));
        BigDecimal pendingCommission = sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PENDING);
        BigDecimal paidCommission = sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PAID);
        long totalSales = sellerSaleRepository.countBySellerId(seller.getId());

        return SellerDashboardDTO.builder()
                .sellerId(seller.getId())
                .slug(seller.getSlug())
                .referralLink(referralLinkBuilder.build(seller.getSlug()))
                .commissionRate(seller.getCommissionRate())
                .status(seller.getStatus())
                .pixKey(seller.getPixKey())
                .memberSince(seller.getCreatedAt())
                .totalSales(totalSales)
                .totalCommission(totalCommission)
                .pendingCommission(pendingCommission)
                .paidCommission(paidCommission)
                .recentSales(recentSales)
                .build();
    }
}
