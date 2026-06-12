package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.OwnerType;
import com.consultafacil.domain.enums.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillingPaymentRepository extends JpaRepository<BillingPayment, String> {
    List<BillingPayment> findByOwnerTypeAndOwnerId(OwnerType ownerType, String ownerId);
    List<BillingPayment> findByPayerIdOrderByCreatedAtDesc(String payerId);
    Page<BillingPayment> findByPaymentTypeAndStatus(PaymentType type, BillingPaymentStatus status, Pageable pageable);
    Optional<BillingPayment> findByGatewayPaymentId(String gatewayPaymentId);
}
