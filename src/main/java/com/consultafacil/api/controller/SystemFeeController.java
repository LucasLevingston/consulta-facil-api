package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.api.dto.billing.systemfee.UpdateSystemFeeDTO;
import com.consultafacil.application.port.in.SystemFeeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/billing/system-fees")
public class SystemFeeController {

    private final SystemFeeUseCase systemFeeUseCase;

    @GetMapping
    @PreAuthorize("@policy.canManagePlans(authentication)")
    public ResponseEntity<List<SystemFeeResponseDTO>> listAll() {
        return ResponseEntity.ok(systemFeeUseCase.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@policy.canManagePlans(authentication)")
    public ResponseEntity<SystemFeeResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(systemFeeUseCase.getById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@policy.canManagePlans(authentication)")
    public ResponseEntity<SystemFeeResponseDTO> update(@PathVariable String id, @RequestBody UpdateSystemFeeDTO dto) {
        return ResponseEntity.ok(systemFeeUseCase.update(id, dto));
    }
}
