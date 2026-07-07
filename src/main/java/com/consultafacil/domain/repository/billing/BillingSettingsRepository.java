package com.consultafacil.domain.repository.billing;

import com.consultafacil.domain.entity.BillingSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingSettingsRepository extends JpaRepository<BillingSettings, String> {
}
