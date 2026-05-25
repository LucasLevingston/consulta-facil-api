package com.example.consulta.application.service;

import com.example.consulta.api.dto.procedurerequest.CreateProcedureRequestDTO;
import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.ProcedureRequest;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.ProfessionalService;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.example.consulta.domain.repository.ProcedureRequestRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.domain.repository.ProfessionalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProcedureRequestService {

    private final ProcedureRequestRepository procedureRequestRepository;
    private final ProfessionalServiceRepository professionalServiceRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final PatientProfileRepository patientProfileRepository;

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

        return toResponseDTO(procedureRequestRepository.save(request));
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
