package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.QrCheckInTokenDTO;
import com.example.consulta.application.port.in.GenerateCheckInTokenUseCase;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.repository.AppointmentRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateCheckInTokenService implements GenerateCheckInTokenUseCase {

    private final AppointmentRepository appointmentRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public QrCheckInTokenDTO execute(String appointmentId, String patientUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (!appointment.getPatient().getUser().getId().equals(patientUserId)) {
            throw new BadRequestException("Appointment does not belong to this patient");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED
                && appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Appointment is not in a check-in eligible status");
        }

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(appointmentId)
                .claims(Map.of("type", "CHECKIN"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();

        return QrCheckInTokenDTO.builder()
                .appointmentId(appointmentId)
                .token(token)
                .build();
    }
}
