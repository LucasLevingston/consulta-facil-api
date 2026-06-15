package com.consultafacil.api.controller;

import com.consultafacil.api.dto.fees.FeeCalculationResponseDTO;
import com.consultafacil.api.dto.fees.FeeConfigDTO;
import com.consultafacil.application.port.in.CalculateFeesUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import com.consultafacil.domain.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/fees")
@RequiredArgsConstructor
@Tag(name = "Fees", description = "Fee calculation endpoints")
public class FeeCalculatorController {

    private final CalculateFeesUseCase calculateFees;

    @GetMapping("/config")
    @PreAuthorize("@policy.canCalculateFees(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get fee config for the authenticated professional's plan")
    public ResponseEntity<FeeConfigDTO> getConfig(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(calculateFees.getConfig(userDetails.getUserId()));
    }

    @GetMapping("/calculate")
    @PreAuthorize("@policy.canCalculateFees(authentication)")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Calculate fee breakdown for a given amount and payment method")
    public ResponseEntity<FeeCalculationResponseDTO> calculate(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "PIX") PaymentMethod paymentMethod,
            @RequestParam(defaultValue = "true") boolean professionalAbsorbsFees,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                calculateFees.calculate(amount, paymentMethod, professionalAbsorbsFees, userDetails.getUserId()));
    }
}
