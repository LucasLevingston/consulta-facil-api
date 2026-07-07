package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.feature.CreateFeatureDTO;
import com.consultafacil.api.dto.billing.feature.FeatureResponseDTO;
import com.consultafacil.api.dto.billing.feature.UpdateFeatureDTO;
import com.consultafacil.application.port.in.feature.CreateFeatureUseCase;
import com.consultafacil.application.port.in.feature.DeleteFeatureUseCase;
import com.consultafacil.application.port.in.feature.GetFeatureByIdUseCase;
import com.consultafacil.application.port.in.feature.ListFeaturesUseCase;
import com.consultafacil.application.port.in.feature.UpdateFeatureUseCase;
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

    private final ListFeaturesUseCase listFeaturesUseCase;
    private final GetFeatureByIdUseCase getFeatureByIdUseCase;
    private final CreateFeatureUseCase createFeatureUseCase;
    private final UpdateFeatureUseCase updateFeatureUseCase;
    private final DeleteFeatureUseCase deleteFeatureUseCase;

    @GetMapping
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<FeatureResponseDTO>> listAll() {
        return ResponseEntity.ok(listFeaturesUseCase.execute());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<FeatureResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(getFeatureByIdUseCase.execute(id));
    }

    @PostMapping
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<FeatureResponseDTO> create(@Valid @RequestBody CreateFeatureDTO dto) {
        return ResponseEntity.ok(createFeatureUseCase.execute(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<FeatureResponseDTO> update(@PathVariable String id, @RequestBody UpdateFeatureDTO dto) {
        return ResponseEntity.ok(updateFeatureUseCase.execute(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteFeatureUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
