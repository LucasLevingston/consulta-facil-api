package com.consultafacil.domain.port.out.billing;

import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.OwnerType;

import java.util.List;
import java.util.Optional;

public interface BillingPaymentRepositoryPort {
    BillingPayment save(BillingPayment payment);
    Optional<BillingPayment> findById(String id);
    List<BillingPayment> findByOwnerId(OwnerType ownerType, String ownerId);
    List<BillingPayment> findByPayerId(String payerId);
    Optional<BillingPayment> findByGatewayPaymentId(String gatewayPaymentId);
    List<BillingPayment> findAll();
}
