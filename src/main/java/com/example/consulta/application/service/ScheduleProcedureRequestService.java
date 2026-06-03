package com.example.consulta.application.service;

import com.example.consulta.application.observability.BusinessMetrics;
import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.example.consulta.api.dto.procedurerequest.ScheduleProcedureRequestDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.ProcedureRequest;
import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.ProcedureRequestStatus;
import com.example.consulta.domain.port.out.AppointmentRepositoryPort;
import com.example.consulta.domain.port.out.PatientProfileRepositoryPort;
import com.example.consulta.domain.port.out.ProcedureRequestRepositoryPort;
import com.example.consulta.application.port.in.ScheduleProcedureRequestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleProcedureRequestService implements ScheduleProcedureRequestUseCase {

    private final ProcedureRequestRepositoryPort procedureRequestRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final BusinessMetrics businessMetrics;

    @Transactional
    public ProcedureRequestResponseDTO execute(String requestId, String patientUserId, ScheduleProcedureRequestDTO dto) {
        ProcedureRequest request = procedureRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ProcedureRequest", requestId));

        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile not found for user: " + patientUserId));

        if (!request.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("This procedure request does not belong to you");
        }

        if (request.getStatus() != ProcedureRequestStatus.PENDING) {
            throw new BadRequestException("Only pending procedure requests can be scheduled");
        }

        Appointment appointment = Appointment.builder()
                .patient(request.getPatient())
                .professional(request.getProfessional())
                .scheduledAt(dto.getScheduledAt())
                .reason(request.getService().getName())
                .modality(dto.getModality() != null ? dto.getModality() : AppointmentModality.IN_PERSON)
                .status(AppointmentStatus.PENDING)
                .service(request.getService())
                .paymentAmount(request.getService().getPrice())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        request.setAppointment(savedAppointment);
        request.setStatus(ProcedureRequestStatus.SCHEDULED);
        ProcedureRequestResponseDTO response = CreateProcedureRequestService.toResponseDTO(procedureRequestRepository.save(request));
        businessMetrics.recordProcedureRequestScheduled();
        return response;
    }
}
