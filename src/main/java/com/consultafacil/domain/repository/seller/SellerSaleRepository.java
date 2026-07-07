package com.consultafacil.domain.repository.seller;

import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.enums.SellerSaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SellerSaleRepository extends JpaRepository<SellerSale, String> {
    List<SellerSale> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<SellerSale> findBySellerIdAndMonthReference(String sellerId, LocalDate monthReference);
    List<SellerSale> findByMonthReference(LocalDate monthReference);
    Optional<SellerSale> findBySubscriptionId(String subscriptionId);

    @Query("SELECT COALESCE(SUM(s.commissionAmount), 0) FROM SellerSale s WHERE s.seller.id = :sellerId AND s.status = :status")
    BigDecimal sumCommissionBySeller(@Param("sellerId") String sellerId, @Param("status") SellerSaleStatus status);

    @Query("SELECT COUNT(s) FROM SellerSale s WHERE s.seller.id = :sellerId")
    long countBySellerId(@Param("sellerId") String sellerId);
}
