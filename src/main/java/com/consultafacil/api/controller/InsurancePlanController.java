package com.consultafacil.api.controller;

import com.consultafacil.api.dto.insurance.HealthInsurancePlanDTO;
import com.consultafacil.application.port.in.insurance.ListInsurancePlansUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/insurance")
@RequiredArgsConstructor
@Tag(name = "Insurance", description = "Health insurance plan management")
public class InsurancePlanController {

    private final ListInsurancePlansUseCase listInsurancePlans;

    @GetMapping("/plans")
    @Operation(summary = "List active health insurance plans")
    public ResponseEntity<List<HealthInsurancePlanDTO>> listPlans() {
        return ResponseEntity.ok(listInsurancePlans.execute());
    }
}
