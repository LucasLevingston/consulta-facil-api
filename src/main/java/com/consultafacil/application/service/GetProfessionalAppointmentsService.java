package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.GetProfessionalAppointmentsUseCase;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProfessionalAppointmentsService implements GetProfessionalAppointmentsUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final AppointmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> execute(String professionalId, Pageable pageable) {
        return professionalProfileRepository.findById(professionalId)
                .map(p -> appointmentRepository.findByProfessionalId(professionalId, pageable)
                        .map(mapper::toResponseDTO))
                .orElse(Page.empty(pageable));
    }
}
