package com.consultafacil.domain.port.out.seller;

import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.enums.SellerSaleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SellerSaleRepositoryPort {
    SellerSale save(SellerSale sale);
    Optional<SellerSale> findById(String id);
    Optional<SellerSale> findBySubscriptionId(String subscriptionId);
    List<SellerSale> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<SellerSale> findBySellerIdAndMonthReference(String sellerId, LocalDate monthReference);
    List<SellerSale> findByMonthReference(LocalDate monthReference);
    BigDecimal sumCommissionBySeller(String sellerId, SellerSaleStatus status);
    long countBySellerId(String sellerId);
}
