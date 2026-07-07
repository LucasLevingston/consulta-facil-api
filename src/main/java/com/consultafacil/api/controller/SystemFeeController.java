package com.consultafacil.api.controller;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.api.dto.billing.systemfee.UpdateSystemFeeDTO;
import com.consultafacil.application.port.in.systemfee.GetSystemFeeByIdUseCase;
import com.consultafacil.application.port.in.systemfee.ListSystemFeesUseCase;
import com.consultafacil.application.port.in.systemfee.UpdateSystemFeeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/billing/system-fees")
public class SystemFeeController {

    private final ListSystemFeesUseCase listSystemFeesUseCase;
    private final GetSystemFeeByIdUseCase getSystemFeeByIdUseCase;
    private final UpdateSystemFeeUseCase updateSystemFeeUseCase;

    @GetMapping
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<List<SystemFeeResponseDTO>> listAll() {
        return ResponseEntity.ok(listSystemFeesUseCase.execute());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<SystemFeeResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(getSystemFeeByIdUseCase.execute(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@adminPolicy.canManagePlans(authentication)")
    public ResponseEntity<SystemFeeResponseDTO> update(@PathVariable String id, @RequestBody UpdateSystemFeeDTO dto) {
        return ResponseEntity.ok(updateSystemFeeUseCase.execute(id, dto));
    }
}
