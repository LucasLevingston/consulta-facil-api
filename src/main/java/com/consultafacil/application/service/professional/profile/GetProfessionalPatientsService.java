package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.appointment.PatientSummaryDTO;
import com.consultafacil.application.port.in.GetProfessionalPatientsUseCase;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProfessionalPatientsService implements GetProfessionalPatientsUseCase {

    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PatientSummaryDTO> execute(String professionalId, String search, String sort, int page, int size) {
        String term = search == null ? "" : search.trim();
        Pageable pageable = PageRequest.of(page, size);
        Page<com.consultafacil.domain.PatientSummary> summaries = "name".equals(sort)
                ? appointmentRepository.findProfessionalPatientsByName(professionalId, term, pageable)
                : appointmentRepository.findProfessionalPatientsByRecent(professionalId, term, pageable);
        return summaries.map(ps -> new PatientSummaryDTO(ps.id(), ps.name(), ps.lastAppointment(), ps.totalAppointments()));
    }
}
