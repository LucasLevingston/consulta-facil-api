package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.RateAppointmentUseCase;
import com.consultafacil.application.port.in.command.RateAppointmentCommand;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RateAppointmentService implements RateAppointmentUseCase {
    private final AppointmentRepositoryPort appointmentRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final AppointmentMapper mapper;

    @Override
    @Transactional
    public AppointmentResponseDTO execute(RateAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", command.appointmentId()));

        PatientProfile patient = patientProfileRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile not found for user: " + command.userId()));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("You can only rate your own appointments");
        }

        appointment.rate(command.stars(), command.comment());
        return mapper.toResponseDTO(appointmentRepository.save(appointment));
    }
}
