package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;
import com.consultafacil.application.port.in.FeatureUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/billing/features")
public class FeatureController {

    private final FeatureUseCase featureUseCase;

    @GetMapping
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<FeatureResponseDTO>> listAll() {
        return ResponseEntity.ok(featureUseCase.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<FeatureResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(featureUseCase.getById(id));
    }

    @PostMapping
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<FeatureResponseDTO> create(@Valid @RequestBody CreateFeatureDTO dto) {
        return ResponseEntity.ok(featureUseCase.create(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<FeatureResponseDTO> update(@PathVariable String id, @RequestBody UpdateFeatureDTO dto) {
        return ResponseEntity.ok(featureUseCase.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        featureUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
