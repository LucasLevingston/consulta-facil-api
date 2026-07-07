package com.consultafacil.application.service.procedurerequest;

import com.consultafacil.application.observability.BusinessMetrics;
import com.consultafacil.api.dto.procedurerequest.CreateProcedureRequestDTO;
import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProcedureRequest;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProcedureRequestRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalServiceRepositoryPort;
import com.consultafacil.application.port.in.CreateProcedureRequestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProcedureRequestService implements CreateProcedureRequestUseCase {

    private final ProcedureRequestRepositoryPort procedureRequestRepository;
    private final ProfessionalServiceRepositoryPort professionalServiceRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final BusinessMetrics businessMetrics;

    @Transactional
    public ProcedureRequestResponseDTO execute(String professionalUserId, CreateProcedureRequestDTO dto) {
        ProfessionalProfile professional = professionalProfileRepository.findByUserId(professionalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile not found for user: " + professionalUserId));

        ProfessionalService service = professionalServiceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalService", dto.getServiceId()));

        if (!service.getProfessional().getId().equals(professional.getId())) {
            throw new BadRequestException("Service does not belong to this professional");
        }

        if (!service.isRequiresConsultation()) {
            throw new BadRequestException("Service does not require a prior consultation — patient can book directly");
        }

        PatientProfile patient = patientProfileRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", dto.getPatientId()));

        ProcedureRequest request = ProcedureRequest.builder()
                .service(service)
                .patient(patient)
                .professional(professional)
                .notes(dto.getNotes())
                .build();

        ProcedureRequestResponseDTO response = toResponseDTO(procedureRequestRepository.save(request));
        businessMetrics.recordProcedureRequestCreated();
        return response;
    }

    static ProcedureRequestResponseDTO toResponseDTO(ProcedureRequest r) {
        return ProcedureRequestResponseDTO.builder()
                .id(r.getId())
                .serviceId(r.getService().getId())
                .serviceName(r.getService().getName())
                .servicePrice(r.getService().getPrice())
                .serviceDurationMinutes(r.getService().getDurationMinutes())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getUser().getName())
                .professionalId(r.getProfessional().getId())
                .professionalName(r.getProfessional().getUser().getName())
                .appointmentId(r.getAppointment() != null ? r.getAppointment().getId() : null)
                .notes(r.getNotes())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
