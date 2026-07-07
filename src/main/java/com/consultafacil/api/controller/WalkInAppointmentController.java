package com.consultafacil.api.controller;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.api.dto.appointment.WalkInAppointmentResponseDTO;
import com.consultafacil.application.port.in.WalkInAppointmentUseCase;
import com.consultafacil.core.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class WalkInAppointmentController {

    private final WalkInAppointmentUseCase walkInAppointmentUseCase;

    @PostMapping("/walk-in")
    @PreAuthorize("@requestPolicy.canRegisterWalkIn(authentication)")
    public ResponseEntity<WalkInAppointmentResponseDTO> createWalkIn(
            @Valid @RequestBody CreateWalkInAppointmentDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WalkInAppointmentResponseDTO response =
                walkInAppointmentUseCase.create(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
