package com.consultafacil.application.service;

import com.consultafacil.api.dto.seller.CreateSellerDTO;
import com.consultafacil.api.dto.seller.SellerDashboardDTO;
import com.consultafacil.api.dto.seller.SellerResponseDTO;
import com.consultafacil.api.dto.seller.SellerSaleResponseDTO;
import com.consultafacil.application.port.in.SellerUseCase;
import com.consultafacil.core.exception.DuplicateResourceException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Seller;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService implements SellerUseCase {

    private final SellerRepositoryPort sellerRepository;
    private final SellerSaleRepositoryPort sellerSaleRepository;
    private final UserRepositoryPort userRepository;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    @Override
    @Transactional
    public SellerResponseDTO createSeller(CreateSellerDTO dto) {
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        if (sellerRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException("Seller", "userId", dto.getUserId());
        }

        Seller seller = Seller.builder()
                .user(userRepository.findById(dto.getUserId()).orElseThrow())
                .slug(generateUniqueSlug())
                .commissionRate(dto.getCommissionRate())
                .pixKey(dto.getPixKey())
                .notes(dto.getNotes())
                .build();

        seller = sellerRepository.save(seller);
        log.info("[Seller] Created seller id={} slug={} for userId={}", seller.getId(), seller.getSlug(), dto.getUserId());
        return toDTO(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerResponseDTO> listSellers() {
        return sellerRepository.findAll().stream()
                .map(this::toDTOWithMetrics)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SellerResponseDTO getSeller(String sellerId) {
        Seller seller = findSellerById(sellerId);
        return toDTOWithMetrics(seller);
    }

    @Override
    @Transactional
    public SellerResponseDTO updateCommissionRate(String sellerId, BigDecimal commissionRate) {
        Seller seller = findSellerById(sellerId);
        seller.setCommissionRate(commissionRate);
        seller = sellerRepository.save(seller);
        log.info("[Seller] Updated commission rate for sellerId={} to {}%", sellerId, commissionRate);
        return toDTOWithMetrics(seller);
    }

    @Override
    @Transactional
    public SellerResponseDTO deactivateSeller(String sellerId) {
        Seller seller = findSellerById(sellerId);
        seller.setStatus(SellerStatus.INACTIVE);
        seller = sellerRepository.save(seller);
        log.info("[Seller] Deactivated sellerId={}", sellerId);
        return toDTO(seller);
    }

    @Override
    @Transactional
    public SellerResponseDTO activateSeller(String sellerId) {
        Seller seller = findSellerById(sellerId);
        seller.setStatus(SellerStatus.ACTIVE);
        seller = sellerRepository.save(seller);
        log.info("[Seller] Activated sellerId={}", sellerId);
        return toDTO(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerSaleResponseDTO> getCommissions(String sellerId) {
        findSellerById(sellerId);
        return sellerSaleRepository.findBySellerIdOrderByCreatedAtDesc(sellerId).stream()
                .map(this::toSaleDTO)
                .toList();
    }

    @Override
    @Transactional
    public SellerSaleResponseDTO updateCommissionStatus(String saleId, SellerSaleStatus status) {
        SellerSale sale = sellerSaleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerSale", saleId));

        sale.setStatus(status);
        if (status == SellerSaleStatus.PAID) {
            sale.setPaidAt(java.time.LocalDateTime.now());
        } else if (status == SellerSaleStatus.REVERSED) {
            sale.setPaidAt(null);
        }

        sale = sellerSaleRepository.save(sale);
        log.info("[Seller] Sale {} status updated to {} for sellerId={}", saleId, status, sale.getSeller().getId());
        return toSaleDTO(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardDTO getMyDashboard(String userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller com userId", userId));

        List<SellerSaleResponseDTO> recentSales = sellerSaleRepository
                .findBySellerIdOrderByCreatedAtDesc(seller.getId()).stream()
                .limit(20)
                .map(this::toSaleDTO)
                .toList();

        BigDecimal totalCommission = sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PENDING)
                .add(sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PAID));
        BigDecimal pendingCommission = sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PENDING);
        BigDecimal paidCommission = sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PAID);
        long totalSales = sellerSaleRepository.countBySellerId(seller.getId());

        return SellerDashboardDTO.builder()
                .sellerId(seller.getId())
                .slug(seller.getSlug())
                .referralLink(buildReferralLink(seller.getSlug()))
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateUniqueSlug() {
        String slug;
        int attempts = 0;
        do {
            slug = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            if (++attempts > 10) {
                throw new IllegalStateException("Failed to generate unique seller slug after 10 attempts");
            }
        } while (sellerRepository.existsBySlug(slug));
        return slug;
    }

    private Seller findSellerById(String sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
    }

    private String buildReferralLink(String slug) {
        return appUrl + "/ref/" + slug;
    }

    private SellerResponseDTO toDTO(Seller seller) {
        return SellerResponseDTO.builder()
                .id(seller.getId())
                .userId(seller.getUser().getId())
                .userName(seller.getUser().getName())
                .userEmail(seller.getUser().getEmail())
                .slug(seller.getSlug())
                .commissionRate(seller.getCommissionRate())
                .status(seller.getStatus())
                .pixKey(seller.getPixKey())
                .notes(seller.getNotes())
                .createdAt(seller.getCreatedAt())
                .referralLink(buildReferralLink(seller.getSlug()))
                .build();
    }

    private SellerResponseDTO toDTOWithMetrics(Seller seller) {
        SellerResponseDTO dto = toDTO(seller);
        dto.setTotalSales(sellerSaleRepository.countBySellerId(seller.getId()));
        dto.setTotalCommission(
                sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PENDING)
                        .add(sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PAID)));
        dto.setPendingCommission(sellerSaleRepository.sumCommissionBySeller(seller.getId(), SellerSaleStatus.PENDING));
        return dto;
    }

    private SellerSaleResponseDTO toSaleDTO(SellerSale sale) {
        return SellerSaleResponseDTO.builder()
                .id(sale.getId())
                .sellerId(sale.getSeller().getId())
                .subscriptionId(sale.getSubscription().getId())
                .grossAmount(sale.getGrossAmount())
                .commissionAmount(sale.getCommissionAmount())
                .monthReference(sale.getMonthReference())
                .status(sale.getStatus())
                .paidAt(sale.getPaidAt())
                .createdAt(sale.getCreatedAt())
                .build();
    }
}
