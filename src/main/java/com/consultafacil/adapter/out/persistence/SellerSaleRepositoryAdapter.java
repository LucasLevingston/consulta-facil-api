package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import com.consultafacil.domain.repository.SellerSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SellerSaleRepositoryAdapter implements SellerSaleRepositoryPort {

    private final SellerSaleRepository sellerSaleRepository;

    @Override
    public SellerSale save(SellerSale sale) {
        return sellerSaleRepository.save(sale);
    }

    @Override
    public Optional<SellerSale> findById(String id) {
        return sellerSaleRepository.findById(id);
    }

    @Override
    public Optional<SellerSale> findBySubscriptionId(String subscriptionId) {
        return sellerSaleRepository.findBySubscriptionId(subscriptionId);
    }

    @Override
    public List<SellerSale> findBySellerIdOrderByCreatedAtDesc(String sellerId) {
        return sellerSaleRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    @Override
    public List<SellerSale> findBySellerIdAndMonthReference(String sellerId, LocalDate monthReference) {
        return sellerSaleRepository.findBySellerIdAndMonthReference(sellerId, monthReference);
    }

    @Override
    public List<SellerSale> findByMonthReference(LocalDate monthReference) {
        return sellerSaleRepository.findByMonthReference(monthReference);
    }

    @Override
    public BigDecimal sumCommissionBySeller(String sellerId, SellerSaleStatus status) {
        return sellerSaleRepository.sumCommissionBySeller(sellerId, status);
    }

    @Override
    public long countBySellerId(String sellerId) {
        return sellerSaleRepository.countBySellerId(sellerId);
    }
}
