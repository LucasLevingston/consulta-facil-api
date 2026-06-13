package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;
import com.consultafacil.application.port.in.CommissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionUseCase commissionUseCase;

    @GetMapping("/admin/billing/commissions")
    @PreAuthorize("@policy.canManageReferrals(authentication)")
    public ResponseEntity<List<ReferralCommissionDTO>> listAll() {
        return ResponseEntity.ok(commissionUseCase.getAllCommissions());
    }
}
