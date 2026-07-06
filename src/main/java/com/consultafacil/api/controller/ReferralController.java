package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.api.dto.billing.referral.ReferralDTO;
import com.consultafacil.api.dto.billing.referral.ReferralStatsDTO;
import com.consultafacil.application.port.in.ReferralUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralUseCase referralUseCase;

    @GetMapping("/referrals/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReferralStatsDTO> myStats(Authentication authentication) {
        return ResponseEntity.ok(referralUseCase.getUserReferralStats(authentication.getName()));
    }

    @PostMapping("/referrals/me/regenerate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReferralCodeDTO> regenerate(Authentication authentication) {
        return ResponseEntity.ok(referralUseCase.regenerateCode(authentication.getName()));
    }

    @PostMapping("/referrals/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> register(@RequestBody Map<String, String> body, Authentication authentication) {
        referralUseCase.registerReferral(authentication.getName(), body.get("code"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/billing/referrals")
    @PreAuthorize("@adminPolicy.canManageReferrals(authentication)")
    public ResponseEntity<List<ReferralDTO>> adminListAll() {
        return ResponseEntity.ok(referralUseCase.getAllReferrals());
    }
}
