package com.consultafacil.core.audit;

import com.consultafacil.domain.entity.FinancialAuditLog;
import com.consultafacil.domain.repository.audit.FinancialAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FinancialAuditRecorder {

    private final FinancialAuditLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(FinancialAuditLog auditLog) {
        repository.save(auditLog);
    }
}
