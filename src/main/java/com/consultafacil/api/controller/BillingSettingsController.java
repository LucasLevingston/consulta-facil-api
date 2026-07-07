package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.settings.BillingSettingsResponseDTO;
import com.consultafacil.api.dto.billing.settings.UpdateBillingSettingsDTO;
import com.consultafacil.application.port.in.billing.GetBillingSettingsUseCase;
import com.consultafacil.application.port.in.billing.UpdateBillingSettingsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/billing/settings")
public class BillingSettingsController {

    private final GetBillingSettingsUseCase getBillingSettings;
    private final UpdateBillingSettingsUseCase updateBillingSettings;

    @GetMapping
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<BillingSettingsResponseDTO> get() {
        return ResponseEntity.ok(getBillingSettings.execute());
    }

    @PatchMapping
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<BillingSettingsResponseDTO> update(@RequestBody UpdateBillingSettingsDTO dto) {
        return ResponseEntity.ok(updateBillingSettings.execute(dto));
    }
}
