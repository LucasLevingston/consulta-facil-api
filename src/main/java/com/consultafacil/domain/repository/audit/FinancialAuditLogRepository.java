package com.consultafacil.domain.repository.audit;

import com.consultafacil.domain.entity.FinancialAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinancialAuditLogRepository extends JpaRepository<FinancialAuditLog, String> {
    List<FinancialAuditLog> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, String entityId);
    List<FinancialAuditLog> findByActorIdOrderByOccurredAtDesc(String actorId);
}
