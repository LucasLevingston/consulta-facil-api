package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.observability.BusinessMetrics;
import com.consultafacil.application.port.in.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.port.out.AppointmentNotificationPort;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalServiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ScheduleAppointmentService implements ScheduleAppointmentUseCase {
    private final AppointmentRepositoryPort appointmentRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalServiceRepositoryPort professionalServiceRepository;
    private final AppointmentNotificationPort appointmentNotification;
    private final AppointmentCheckoutInitiator checkoutInitiator;
    private final BusinessMetrics businessMetrics;
    private final AppointmentMapper mapper;

    @Override
    @Transactional
    public AppointmentResponseDTO execute(ScheduleAppointmentCommand command) {
        PatientProfile patient = findPatient(command.userId());
        ProfessionalProfile professional = findProfessional(command.professionalId());
        assertNoConflict(command);
        ProfessionalService service = resolveService(command);
        PaymentMethod chosenMethod = validatePaymentMethod(professional, command.chosenPaymentMethod());
        BigDecimal paymentAmount = service != null ? service.getPrice() : professional.getConsultationPrice();
        Appointment appointment = Appointment.schedule(
                patient, professional, command.scheduledAt(), command.reason(), command.notes(),
                command.modality() != null ? command.modality() : AppointmentModality.IN_PERSON,
                service, paymentAmount, chosenMethod);
        Appointment saved = appointmentRepository.save(appointment);
        appointmentNotification.notifyScheduled(saved);
        businessMetrics.recordAppointmentCreated();
        AppointmentResponseDTO response = mapper.toResponseDTO(saved);
        checkoutInitiator.maybeInitiateCheckout(
                saved, professional, command.userId(), paymentAmount, chosenMethod, response);
        response.setPaymentTiming(professional.getPaymentTiming());
        return response;
    }

    private PatientProfile findPatient(String userId) {
        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile not found for user: " + userId));
    }

    private ProfessionalProfile findProfessional(String professionalId) {
        return professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));
    }

    private void assertNoConflict(ScheduleAppointmentCommand command) {
        if (appointmentRepository.existsByProfessionalIdAndScheduledAt(
                command.professionalId(), command.scheduledAt())) {
            throw new BadRequestException("Professional already has an appointment scheduled at this time");
        }
    }

    private ProfessionalService resolveService(ScheduleAppointmentCommand command) {
        if (command.serviceId() == null || command.serviceId().isBlank()) {
            return null;
        }
        ProfessionalService service = professionalServiceRepository.findById(command.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalService", command.serviceId()));
        if (service.isRequiresConsultation()) {
            throw new BadRequestException(
                    "This service requires a prior consultation and cannot be scheduled directly.");
        }
        return service;
    }

    private PaymentMethod validatePaymentMethod(ProfessionalProfile professional, PaymentMethod chosenMethod) {
        if (chosenMethod != null && !professional.getAcceptedPaymentMethods().isEmpty()
                && !professional.getAcceptedPaymentMethods().contains(chosenMethod)) {
            throw new BadRequestException("Payment method not accepted by this professional: " + chosenMethod);
        }
        return chosenMethod;
    }
}
