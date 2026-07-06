package com.consultafacil.api.controller;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.api.dto.plan.UpdatePlanDTO;
import com.consultafacil.application.port.in.PlanUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanUseCase planUseCase;

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponseDTO>> listActivePlans() {
        return ResponseEntity.ok(planUseCase.listActivePlans());
    }

    @GetMapping("/plans/{slug}")
    public ResponseEntity<PlanResponseDTO> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(planUseCase.getBySlug(slug));
    }

    @GetMapping("/admin/plans")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<PlanResponseDTO>> listAllPlans() {
        return ResponseEntity.ok(planUseCase.listAllPlans());
    }

    @PostMapping("/admin/plans")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<PlanResponseDTO> createPlan(@Valid @RequestBody CreatePlanDTO dto) {
        return ResponseEntity.ok(planUseCase.createPlan(dto));
    }

    @PatchMapping("/admin/plans/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<PlanResponseDTO> updatePlan(
            @PathVariable String id,
            @RequestBody UpdatePlanDTO dto) {
        return ResponseEntity.ok(planUseCase.updatePlan(id, dto));
    }
}
