package com.consultafacil.application.service.procedurerequest;

import com.consultafacil.application.observability.BusinessMetrics;
import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.consultafacil.api.dto.procedurerequest.ScheduleProcedureRequestDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProcedureRequest;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.ProcedureRequestStatus;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.procedurerequest.ProcedureRequestRepositoryPort;
import com.consultafacil.application.port.in.procedurerequest.ScheduleProcedureRequestUseCase;
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
