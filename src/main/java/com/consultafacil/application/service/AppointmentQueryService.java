package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.PatientSummaryDTO;
import com.consultafacil.api.dto.professional.ProfessionalRatingDTO;
import com.consultafacil.application.port.in.AppointmentQueryUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentQueryService implements AppointmentQueryUseCase {
    private final AppointmentRepositoryPort appointmentRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final OwnershipValidator ownershipValidator;
    private final AppointmentMapper mapper;
    private final RatingDistributionBuilder ratingDistributionBuilder;

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getById(String appointmentId, String authenticatedUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return mapper.toResponseDTO(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getPatientAppointments(String userId, String authenticatedUserId,
                                                                boolean isAdmin, Pageable pageable) {
        if (!isAdmin && !userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("You can only view your own appointments");
        }
        return patientProfileRepository.findByUserId(userId)
                .map(patient -> appointmentRepository.findByPatientId(patient.getId(), pageable)
                        .map(mapper::toResponseDTO))
                .orElse(Page.empty(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getProfessionalAppointments(String professionalId, Pageable pageable) {
        return professionalProfileRepository.findById(professionalId)
                .map(p -> appointmentRepository.findByProfessionalId(professionalId, pageable)
                        .map(mapper::toResponseDTO))
                .orElse(Page.empty(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getProfessionalAppointmentsBySource(String professionalId,
                                                                             AppointmentSource source,
                                                                             Pageable pageable) {
        return professionalProfileRepository.findById(professionalId)
                .map(p -> appointmentRepository.findByProfessionalIdAndSource(professionalId, source, pageable)
                        .map(mapper::toResponseDTO))
                .orElse(Page.empty(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientSummaryDTO> getProfessionalPatients(String professionalId, String search,
                                                            String sort, int page, int size) {
        String term = search == null ? "" : search.trim();
        Pageable pageable = PageRequest.of(page, size);
        Page<com.consultafacil.domain.PatientSummary> summaries = "name".equals(sort)
                ? appointmentRepository.findProfessionalPatientsByName(professionalId, term, pageable)
                : appointmentRepository.findProfessionalPatientsByRecent(professionalId, term, pageable);
        return summaries.map(ps -> new PatientSummaryDTO(ps.id(), ps.name(), ps.lastAppointment(), ps.totalAppointments()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(mapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalRatingDTO getProfessionalRatings(String professionalId) {
        Double average = appointmentRepository.findAverageRatingByProfessionalId(professionalId);
        List<Object[]> rows = appointmentRepository.findRatingDistributionByProfessionalId(professionalId);
        return ratingDistributionBuilder.build(average, rows);
    }
}
