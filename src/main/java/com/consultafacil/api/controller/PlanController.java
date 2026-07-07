package com.consultafacil.api.controller;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.api.dto.plan.UpdatePlanDTO;
import com.consultafacil.application.port.in.plan.CreatePlanUseCase;
import com.consultafacil.application.port.in.plan.GetPlanBySlugUseCase;
import com.consultafacil.application.port.in.plan.ListActivePlansUseCase;
import com.consultafacil.application.port.in.plan.ListAllPlansUseCase;
import com.consultafacil.application.port.in.plan.UpdatePlanUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final ListActivePlansUseCase listActivePlansUseCase;
    private final ListAllPlansUseCase listAllPlansUseCase;
    private final GetPlanBySlugUseCase getPlanBySlugUseCase;
    private final CreatePlanUseCase createPlanUseCase;
    private final UpdatePlanUseCase updatePlanUseCase;

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponseDTO>> listActivePlans() {
        return ResponseEntity.ok(listActivePlansUseCase.execute());
    }

    @GetMapping("/plans/{slug}")
    public ResponseEntity<PlanResponseDTO> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(getPlanBySlugUseCase.execute(slug));
    }

    @GetMapping("/admin/plans")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<PlanResponseDTO>> listAllPlans() {
        return ResponseEntity.ok(listAllPlansUseCase.execute());
    }

    @PostMapping("/admin/plans")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<PlanResponseDTO> createPlan(@Valid @RequestBody CreatePlanDTO dto) {
        return ResponseEntity.ok(createPlanUseCase.execute(dto));
    }

    @PatchMapping("/admin/plans/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<PlanResponseDTO> updatePlan(
            @PathVariable String id,
            @RequestBody UpdatePlanDTO dto) {
        return ResponseEntity.ok(updatePlanUseCase.execute(id, dto));
    }
}
