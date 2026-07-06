package com.consultafacil.application.service;

import com.consultafacil.application.port.in.DeleteAppointmentUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAppointmentService implements DeleteAppointmentUseCase {
    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    @Transactional
    public void delete(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        appointmentRepository.delete(appointment);
    }
}
