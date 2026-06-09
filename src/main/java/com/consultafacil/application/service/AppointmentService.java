package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.PatientSummaryDTO;
import com.consultafacil.api.dto.appointment.PaymentResponseDTO;
import com.consultafacil.application.port.in.AppointmentQueryUseCase;
import com.consultafacil.application.port.in.CancelAppointmentUseCase;
import com.consultafacil.application.port.in.CompleteAppointmentUseCase;
import com.consultafacil.application.port.in.ConfirmAppointmentUseCase;
import com.consultafacil.application.port.in.DeleteAppointmentUseCase;
import com.consultafacil.application.port.in.RateAppointmentUseCase;
import com.consultafacil.application.port.in.ScheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.CancelAppointmentCommand;
import com.consultafacil.application.port.in.command.RateAppointmentCommand;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.enums.PaymentTiming;
import com.consultafacil.domain.port.out.AppointmentNotificationPort;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalServiceRepositoryPort;
import com.consultafacil.application.observability.BusinessMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService implements
        ScheduleAppointmentUseCase,
        ConfirmAppointmentUseCase,
        CancelAppointmentUseCase,
        CompleteAppointmentUseCase,
        RateAppointmentUseCase,
        DeleteAppointmentUseCase,
        AppointmentQueryUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalServiceRepositoryPort professionalServiceRepository;
    private final AppointmentNotificationPort appointmentNotification;
    private final CreateAppointmentPaymentService createAppointmentPaymentService;
    private final BusinessMetrics businessMetrics;
    private final OwnershipValidator ownershipValidator;

    @Override
    @Transactional
    public AppointmentResponseDTO execute(ScheduleAppointmentCommand command) {
        PatientProfile patient = patientProfileRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile not found for user: " + command.userId()));

        ProfessionalProfile professional = professionalProfileRepository
                .findById(command.professionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Professional", command.professionalId()));

        if (appointmentRepository.existsByProfessionalIdAndScheduledAt(
                command.professionalId(), command.scheduledAt())) {
            throw new BadRequestException(
                    "Professional already has an appointment scheduled at this time");
        }

        ProfessionalService service = null;
        if (command.serviceId() != null && !command.serviceId().isBlank()) {
            service = professionalServiceRepository.findById(command.serviceId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ProfessionalService", command.serviceId()));
            if (service.isRequiresConsultation()) {
                throw new BadRequestException(
                        "This service requires a prior consultation and cannot be scheduled directly.");
            }
        }

        PaymentMethod chosenMethod = command.chosenPaymentMethod();
        if (chosenMethod != null && !professional.getAcceptedPaymentMethods().isEmpty()
                && !professional.getAcceptedPaymentMethods().contains(chosenMethod)) {
            throw new BadRequestException(
                    "Payment method not accepted by this professional: " + chosenMethod);
        }

        BigDecimal paymentAmount = service != null
                ? service.getPrice()
                : professional.getConsultationPrice();

        Appointment appointment = Appointment.schedule(
                patient, professional,
                command.scheduledAt(), command.reason(), command.notes(),
                command.modality() != null ? command.modality() : AppointmentModality.IN_PERSON,
                service, paymentAmount, chosenMethod);

        Appointment saved = appointmentRepository.save(appointment);
        appointmentNotification.notifyScheduled(saved);
        businessMetrics.recordAppointmentCreated();

        AppointmentResponseDTO response = toResponseDTO(saved);

        // Auto-generate checkout when professional requires payment at scheduling and patient chose MercadoPago
        if (professional.getPaymentTiming() == PaymentTiming.AT_SCHEDULING
                && chosenMethod == PaymentMethod.MERCADOPAGO
                && paymentAmount != null) {
            try {
                PaymentResponseDTO checkout = createAppointmentPaymentService.execute(
                        saved.getId(), command.userId(), paymentAmount);
                response.setCheckoutUrl(checkout.getCheckoutUrl());
            } catch (Exception e) {
                log.error("Failed to auto-generate checkout for appointment {}: {}",
                        saved.getId(), e.getMessage());
            }
        }

        response.setPaymentTiming(professional.getPaymentTiming());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getById(String appointmentId, String authenticatedUserId) {
        log.debug("Fetching appointment by ID: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return toResponseDTO(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getPatientAppointments(String userId, String authenticatedUserId,
                                                                boolean isAdmin, Pageable pageable) {
        log.debug("Fetching appointments for user: {}", userId);
        if (!isAdmin && !userId.equals(authenticatedUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only view your own appointments");
        }
        return patientProfileRepository.findByUserId(userId)
                .map(patient -> appointmentRepository.findByPatientId(patient.getId(), pageable)
                        .map(this::toResponseDTO))
                .orElse(Page.empty(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getProfessionalAppointments(String professionalId,
                                                                     Pageable pageable) {
        log.debug("Fetching appointments for professional: {}", professionalId);
        return professionalProfileRepository.findById(professionalId)
                .map(p -> appointmentRepository.findByProfessionalId(professionalId, pageable)
                        .map(this::toResponseDTO))
                .orElse(Page.empty());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getProfessionalAppointmentsBySource(String professionalId,
                                                                             AppointmentSource source,
                                                                             Pageable pageable) {
        return professionalProfileRepository.findById(professionalId)
                .map(p -> appointmentRepository.findByProfessionalIdAndSource(professionalId, source, pageable)
                        .map(this::toResponseDTO))
                .orElse(Page.empty());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientSummaryDTO> getProfessionalPatients(String professionalId, String search,
                                                            String sort, int page, int size) {
        log.debug("Fetching patients for professional: {}, search: {}, sort: {}",
                professionalId, search, sort);
        String term = search == null ? "" : search.trim();
        Pageable pageable = PageRequest.of(page, size);
        Page<com.consultafacil.domain.PatientSummary> summaries = "name".equals(sort)
                ? appointmentRepository.findProfessionalPatientsByName(professionalId, term, pageable)
                : appointmentRepository.findProfessionalPatientsByRecent(professionalId, term, pageable);
        return summaries.map(ps -> new PatientSummaryDTO(ps.id(), ps.name(), ps.lastAppointment(), ps.totalAppointments()));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO confirm(String appointmentId, String professionalUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyProfessionalOwnership(appointment, professionalUserId);
        appointment.confirm();
        Appointment updated = appointmentRepository.save(appointment);
        appointmentNotification.notifyConfirmed(updated);
        return toResponseDTO(updated);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO complete(String appointmentId, String professionalUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyProfessionalOwnership(appointment, professionalUserId);
        appointment.complete();
        return toResponseDTO(appointmentRepository.save(appointment));
    }

    // --- kept for backward-compat if anything still calls old names ---

    @Transactional
    public AppointmentResponseDTO confirmAppointment(String appointmentId, String userId) {
        return confirm(appointmentId, userId);
    }

    @Transactional
    public AppointmentResponseDTO completeAppointment(String appointmentId, String userId) {
        return complete(appointmentId, userId);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO execute(CancelAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", command.appointmentId()));
        ownershipValidator.verifyAppointmentAccess(appointment, command.authenticatedUserId());
        appointment.cancel(command.cancellationReason());
        Appointment updated = appointmentRepository.save(appointment);
        appointmentNotification.notifyCanceled(updated);
        businessMetrics.recordAppointmentCanceled();
        return toResponseDTO(updated);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO execute(RateAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", command.appointmentId()));

        PatientProfile patient = patientProfileRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile not found for user: " + command.userId()));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("You can only rate your own appointments");
        }

        appointment.rate(command.stars(), command.comment());
        return toResponseDTO(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public void delete(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        appointmentRepository.delete(appointment);
    }

    // --- backward-compat bridges (old callers: WhatsAppAgentService, DatabaseSeeder) ---

    @Transactional
    public AppointmentResponseDTO scheduleAppointment(String userId,
                                                       com.consultafacil.api.dto.appointment.CreateAppointmentDTO dto) {
        return execute(new ScheduleAppointmentCommand(
                userId,
                dto.getProfessionalId(),
                dto.getScheduledAt(),
                dto.getReason(),
                dto.getNotes(),
                dto.getModality(),
                dto.getServiceId(),
                dto.getChosenPaymentMethod()));
    }

    @Transactional
    public AppointmentResponseDTO cancelAppointment(String appointmentId, String authenticatedUserId,
                                                     com.consultafacil.api.dto.appointment.CancelAppointmentDTO dto) {
        return execute(new CancelAppointmentCommand(appointmentId, authenticatedUserId, dto.getCancellationReason()));
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(String appointmentId, String authenticatedUserId) {
        return getById(appointmentId, authenticatedUserId);
    }

    @Transactional
    public void deleteAppointment(String appointmentId) {
        delete(appointmentId);
    }

    @Transactional
    public AppointmentResponseDTO rateAppointment(String appointmentId, String userId,
                                                   com.consultafacil.api.dto.appointment.RateAppointmentDTO dto) {
        return execute(new RateAppointmentCommand(appointmentId, userId, dto.getStars(), dto.getComment()));
    }

    // --- shared DTO mapper ---

    public AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient() != null && appointment.getPatient().getUser() != null
                        ? appointment.getPatient().getUser().getName() : null)
                .patientId(appointment.getPatient() != null ? appointment.getPatient().getId() : null)
                .professionalName(appointment.getProfessional() != null && appointment.getProfessional().getUser() != null
                        ? appointment.getProfessional().getUser().getName() : null)
                .professionalId(appointment.getProfessional() != null ? appointment.getProfessional().getId() : null)
                .specialty(appointment.getProfessional() != null ? appointment.getProfessional().getSpecialty() : null)
                .scheduledAt(appointment.getScheduledAt())
                .previousScheduledAt(appointment.getPreviousScheduledAt())
                .checkedInAt(appointment.getCheckedInAt())
                .calledAt(appointment.getCalledAt())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .modality(appointment.getModality())
                .meetLink(appointment.getMeetLink())
                .status(appointment.getStatus())
                .cancellationReason(appointment.getCancellationReason())
                .paymentStatus(appointment.getPaymentStatus())
                .paymentAmount(appointment.getPaymentAmount())
                .chosenPaymentMethod(appointment.getChosenPaymentMethod())
                .paymentTiming(appointment.getProfessional() != null ? appointment.getProfessional().getPaymentTiming() : null)
                .rating(appointment.getRating())
                .ratingComment(appointment.getRatingComment())
                .serviceId(appointment.getService() != null ? appointment.getService().getId() : null)
                .serviceName(appointment.getService() != null ? appointment.getService().getName() : null)
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
