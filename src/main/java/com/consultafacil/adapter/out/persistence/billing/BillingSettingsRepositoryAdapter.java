package com.consultafacil.adapter.out.persistence.billing;

import com.consultafacil.domain.entity.BillingSettings;
import com.consultafacil.domain.port.out.billing.BillingSettingsRepositoryPort;
import com.consultafacil.domain.repository.billing.BillingSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BillingSettingsRepositoryAdapter implements BillingSettingsRepositoryPort {

    private final BillingSettingsRepository billingSettingsRepository;

    @Override
    public BillingSettings save(BillingSettings settings) { return billingSettingsRepository.save(settings); }

    @Override
    public Optional<BillingSettings> findFirst() {
        return billingSettingsRepository.findAll().stream().findFirst();
    }
}
