package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReferralCommissionRepository extends JpaRepository<ReferralCommission, String> {
    Optional<ReferralCommission> findByPaymentId(String paymentId);
    List<ReferralCommission> findByStatus(CommissionStatus status);
    List<ReferralCommission> findByStatusAndAvailableAtBefore(CommissionStatus status, LocalDateTime now);
    boolean existsByReferralIdAndPaymentId(String referralId, String paymentId);
}
