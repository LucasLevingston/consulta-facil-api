package com.consultafacil.api.controller;

import com.consultafacil.api.dto.dependent.CreateDependentDTO;
import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.api.dto.dependent.UpdateDependentDTO;
import com.consultafacil.application.port.in.CreateDependentUseCase;
import com.consultafacil.application.port.in.DeleteDependentUseCase;
import com.consultafacil.application.port.in.ListDependentsByGuardianUseCase;
import com.consultafacil.application.port.in.UpdateDependentUseCase;
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

    private final CreateDependentUseCase createDependent;
    private final ListDependentsByGuardianUseCase listDependentsByGuardian;
    private final UpdateDependentUseCase updateDependent;
    private final DeleteDependentUseCase deleteDependent;

    @PostMapping("/users/me/dependents")
    @PreAuthorize("@carePolicy.canManageDependents(authentication)")
    public ResponseEntity<DependentResponseDTO> create(
            @Valid @RequestBody CreateDependentDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createDependent.execute(userDetails.getUsername(), dto));
    }

    @GetMapping("/users/me/dependents")
    @PreAuthorize("@carePolicy.canManageDependents(authentication)")
    public ResponseEntity<List<DependentResponseDTO>> list(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(listDependentsByGuardian.execute(userDetails.getUsername()));
    }

    @PutMapping("/dependents/{dependentId}")
    @PreAuthorize("@carePolicy.canManageDependents(authentication)")
    public ResponseEntity<DependentResponseDTO> update(
            @PathVariable String dependentId,
            @Valid @RequestBody UpdateDependentDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(updateDependent.execute(dependentId, userDetails.getUsername(), dto));
    }

    @DeleteMapping("/dependents/{dependentId}")
    @PreAuthorize("@carePolicy.canManageDependents(authentication)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String dependentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        deleteDependent.execute(dependentId, userDetails.getUsername());
    }
}
