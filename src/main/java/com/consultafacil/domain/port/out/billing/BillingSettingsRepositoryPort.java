package com.consultafacil.domain.port.out.billing;

import com.consultafacil.domain.entity.BillingSettings;

import java.util.Optional;

public interface BillingSettingsRepositoryPort {
    BillingSettings save(BillingSettings settings);
    Optional<BillingSettings> findFirst();
}
