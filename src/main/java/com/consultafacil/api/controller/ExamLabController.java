package com.consultafacil.api.controller;

import com.consultafacil.api.dto.examlab.AvailableSlotDTO;
import com.consultafacil.api.dto.examlab.CreateExamLabDTO;
import com.consultafacil.api.dto.examlab.ExamLabHoursDTO;
import com.consultafacil.api.dto.examlab.ExamLabResponseDTO;
import com.consultafacil.application.port.in.CreateExamLabUseCase;
import com.consultafacil.application.port.in.GetAvailableSlotsUseCase;
import com.consultafacil.application.port.in.GetExamLabsUseCase;
import com.consultafacil.application.port.in.GetNearbyExamLabsUseCase;
import com.consultafacil.application.port.in.SetExamLabHoursUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exam-labs")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Exam Labs", description = "Exam lab browsing and scheduling")
public class ExamLabController {

    private final CreateExamLabUseCase createExamLab;
    private final GetExamLabsUseCase getExamLabs;
    private final GetNearbyExamLabsUseCase getNearbyExamLabs;
    private final SetExamLabHoursUseCase setExamLabHours;
    private final GetAvailableSlotsUseCase getAvailableSlots;

    @PostMapping
    @PreAuthorize("@policy.canManageExamLab(authentication)")
    @Operation(summary = "Create an exam lab (admin)")
    public ResponseEntity<ExamLabResponseDTO> create(@Valid @RequestBody CreateExamLabDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createExamLab.execute(dto));
    }

    @GetMapping
    @PreAuthorize("@policy.canViewExamLabs(authentication)")
    @Operation(summary = "List all active exam labs")
    public ResponseEntity<List<ExamLabResponseDTO>> getAll() {
        return ResponseEntity.ok(getExamLabs.execute());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@policy.canViewExamLabs(authentication)")
    @Operation(summary = "Get exam lab by ID")
    public ResponseEntity<ExamLabResponseDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(getExamLabs.executeById(id));
    }

    @GetMapping("/nearby")
    @PreAuthorize("@policy.canViewExamLabs(authentication)")
    @Operation(summary = "Find nearby exam labs")
    public ResponseEntity<List<ExamLabResponseDTO>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm) {
        return ResponseEntity.ok(getNearbyExamLabs.execute(lat, lng, radiusKm));
    }

    @PutMapping("/{id}/hours")
    @PreAuthorize("@policy.canManageExamLab(authentication)")
    @Operation(summary = "Set operating hours for an exam lab (admin)")
    public ResponseEntity<ExamLabResponseDTO> setHours(
            @PathVariable String id,
            @Valid @RequestBody List<ExamLabHoursDTO> hours) {
        return ResponseEntity.ok(setExamLabHours.execute(id, hours));
    }

    @GetMapping("/{id}/available-slots")
    @PreAuthorize("@policy.canViewExamLabs(authentication)")
    @Operation(summary = "Get available time slots for an exam lab on a given date")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(getAvailableSlots.execute(id, date));
    }
}
