package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.GetAllAppointmentsUseCase;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAllAppointmentsService implements GetAllAppointmentsUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final AppointmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> execute(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(mapper::toResponseDTO);
    }
}
