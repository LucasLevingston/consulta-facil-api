package com.consultafacil.core.audit;

import com.consultafacil.core.security.CustomUserDetails;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.FinancialAuditLog;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.entity.WalletTransaction;
import com.consultafacil.domain.enums.FinancialAuditAction;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

@Slf4j
public class FinancialAuditListener {

    @PostPersist
    protected void onCreated(Object entity) {
        record(entity, FinancialAuditAction.CREATED);
    }

    @PostUpdate
    protected void onUpdated(Object entity) {
        record(entity, FinancialAuditAction.UPDATED);
    }

    private void record(Object entity, FinancialAuditAction action) {
        try {
            FinancialAuditRecorder recorder = ApplicationContextHolder.getBean(FinancialAuditRecorder.class);
            if (recorder == null) return;

            String entityType;
            String entityId;
            BigDecimal amount;

            if (entity instanceof WalletTransaction wt) {
                entityType = "WALLET_TRANSACTION";
                entityId = wt.getId();
                amount = wt.getAmount();
            } else if (entity instanceof ReferralCommission rc) {
                entityType = "REFERRAL_COMMISSION";
                entityId = rc.getId();
                amount = rc.getAmount();
            } else if (entity instanceof BillingPayment bp) {
                entityType = "BILLING_PAYMENT";
                entityId = bp.getId();
                amount = bp.getAmount();
            } else {
                return;
            }

            recorder.record(FinancialAuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .actorId(resolveActorId())
                    .amount(amount)
                    .build());
        } catch (Exception e) {
            log.warn("Financial audit log failed (isolated): entity={} action={} error={}",
                    entity.getClass().getSimpleName(), action, e.getMessage());
        }
    }

    private String resolveActorId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                return userDetails.getUserId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
