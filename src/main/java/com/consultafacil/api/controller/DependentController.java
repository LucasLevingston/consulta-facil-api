package com.consultafacil.api.controller;

import com.consultafacil.api.dto.dependent.CreateDependentDTO;
import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.api.dto.dependent.UpdateDependentDTO;
import com.consultafacil.application.port.in.DependentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DependentController {

    private final DependentUseCase dependentUseCase;

    @PostMapping("/users/me/dependents")
    @PreAuthorize("@policy.canManageDependents(authentication)")
    public ResponseEntity<DependentResponseDTO> create(
            @Valid @RequestBody CreateDependentDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dependentUseCase.create(userDetails.getUsername(), dto));
    }

    @GetMapping("/users/me/dependents")
    @PreAuthorize("@policy.canManageDependents(authentication)")
    public ResponseEntity<List<DependentResponseDTO>> list(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dependentUseCase.listByGuardian(userDetails.getUsername()));
    }

    @PutMapping("/dependents/{dependentId}")
    @PreAuthorize("@policy.canManageDependents(authentication)")
    public ResponseEntity<DependentResponseDTO> update(
            @PathVariable String dependentId,
            @Valid @RequestBody UpdateDependentDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dependentUseCase.update(dependentId, userDetails.getUsername(), dto));
    }

    @DeleteMapping("/dependents/{dependentId}")
    @PreAuthorize("@policy.canManageDependents(authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String dependentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        dependentUseCase.delete(dependentId, userDetails.getUsername());
    }
}
