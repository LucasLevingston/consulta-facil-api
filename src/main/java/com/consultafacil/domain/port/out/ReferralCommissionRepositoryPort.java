package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.CommissionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReferralCommissionRepositoryPort {
    ReferralCommission save(ReferralCommission commission);
    Optional<ReferralCommission> findByPaymentId(String paymentId);
    List<ReferralCommission> findByStatus(CommissionStatus status);
    List<ReferralCommission> findByStatusAndAvailableAtBefore(CommissionStatus status, LocalDateTime now);
    boolean existsByReferralIdAndPaymentId(String referralId, String paymentId);
    List<ReferralCommission> findAll();
}
