package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.appointment.CancelAppointmentDTO;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.api.dto.appointment.PaymentResponseDTO;
import com.example.consulta.api.dto.appointment.RescheduleAppointmentDTO;
import com.example.consulta.api.dto.appointment.PatientSummaryDTO;
import com.example.consulta.api.dto.appointment.RateAppointmentDTO;
import com.example.consulta.domain.entity.ProfessionalService;
import com.example.consulta.domain.repository.ProfessionalServiceRepository;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.core.security.OwnershipValidator;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.enums.AppointmentPaymentStatus;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.PaymentMethod;
import com.example.consulta.domain.enums.PaymentTiming;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.example.consulta.application.observability.BusinessMetrics;
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
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final ProfessionalServiceRepository professionalServiceRepository;
    private final AppointmentNotificationService appointmentNotificationService;
    private final CreateAppointmentPaymentService createAppointmentPaymentService;
    private final BusinessMetrics businessMetrics;
    private final OwnershipValidator ownershipValidator;

    @Transactional
    public AppointmentResponseDTO scheduleAppointment(String userId, CreateAppointmentDTO dto) {

        PatientProfile patient = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        ProfessionalProfile professional = professionalProfileRepository.findById(dto.getProfessionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Professional", dto.getProfessionalId()));

        if (appointmentRepository.existsByProfessionalIdAndScheduledAt(dto.getProfessionalId(), dto.getScheduledAt())) {
            throw new BadRequestException("Professional already has an appointment scheduled at this time");
        }

        ProfessionalService service = null;
        if (dto.getServiceId() != null && !dto.getServiceId().isBlank()) {
            service = professionalServiceRepository.findById(dto.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProfessionalService", dto.getServiceId()));
        }

        PaymentMethod chosenMethod = dto.getChosenPaymentMethod();
        if (chosenMethod != null && !professional.getAcceptedPaymentMethods().isEmpty()
                && !professional.getAcceptedPaymentMethods().contains(chosenMethod)) {
            throw new BadRequestException("Método de pagamento não aceito por este profissional: " + chosenMethod);
        }

        BigDecimal paymentAmount = service != null
                ? service.getPrice()
                : professional.getConsultationPrice();

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .professional(professional)
                .scheduledAt(dto.getScheduledAt())
                .reason(dto.getReason())
                .notes(dto.getNotes())
                .modality(dto.getModality() != null ? dto.getModality() : AppointmentModality.IN_PERSON)
                .status(AppointmentStatus.PENDING)
                .service(service)
                .paymentAmount(paymentAmount)
                .chosenPaymentMethod(chosenMethod)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        appointmentNotificationService.notifyScheduled(saved);
        businessMetrics.recordAppointmentCreated();

        AppointmentResponseDTO response = toResponseDTO(saved);

        // When professional requires payment at scheduling time and patient chose MercadoPago,
        // generate checkout immediately so the response includes the checkout URL.
        if (professional.getPaymentTiming() == PaymentTiming.AT_SCHEDULING
                && chosenMethod == PaymentMethod.MERCADOPAGO
                && paymentAmount != null) {
            try {
                PaymentResponseDTO checkout = createAppointmentPaymentService.execute(
                        saved.getId(), userId, paymentAmount);
                response.setCheckoutUrl(checkout.getCheckoutUrl());
            } catch (Exception e) {
                log.error("Failed to auto-generate checkout for appointment {}: {}", saved.getId(), e.getMessage());
            }
        }

        response.setPaymentTiming(professional.getPaymentTiming());
        return response;
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(String appointmentId, String authenticatedUserId) {
        log.debug("Fetching appointment by ID: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return toResponseDTO(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getPatientAppointments(String userId, String authenticatedUserId, boolean isAdmin, Pageable pageable) {
        log.debug("Fetching appointments for user: {}", userId);
        if (!isAdmin && !userId.equals(authenticatedUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only view your own appointments");
        }
        return patientProfileRepository.findByUserId(userId)
                .map(patient -> appointmentRepository.findByPatientId(patient.getId(), pageable)
                        .map(this::toResponseDTO))
                .orElse(Page.empty(pageable));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getProfessionalAppointments(String professionalId, Pageable pageable) {
        log.debug("Fetching appointments for professional: {}", professionalId);
        return professionalProfileRepository.findById(professionalId)
                .map(p -> appointmentRepository.findByProfessionalId(professionalId, pageable)
                        .map(this::toResponseDTO))
                .orElse(Page.empty());
    }

    @Transactional
    public AppointmentResponseDTO confirmAppointment(String appointmentId, String professionalUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        ownershipValidator.verifyProfessionalOwnership(appointment, professionalUserId);

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Only pending appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment updated = appointmentRepository.save(appointment);
        appointmentNotificationService.notifyConfirmed(updated);
        return toResponseDTO(updated);
    }

    @Transactional
    public AppointmentResponseDTO cancelAppointment(String appointmentId, String authenticatedUserId, CancelAppointmentDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
                appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new BadRequestException("Cannot cancel an appointment with status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setCancellationReason(dto.getCancellationReason());
        Appointment updated = appointmentRepository.save(appointment);
        appointmentNotificationService.notifyCanceled(updated);
        businessMetrics.recordAppointmentCanceled();
        return toResponseDTO(updated);
    }

    @Transactional
    public AppointmentResponseDTO completeAppointment(String appointmentId, String professionalUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        ownershipValidator.verifyProfessionalOwnership(appointment, professionalUserId);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED
                && appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BadRequestException("Only confirmed or in-progress appointments can be marked as completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment updated = appointmentRepository.save(appointment);
        return toResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public Page<PatientSummaryDTO> getProfessionalPatients(String professionalId, String search, String sort, int page, int size) {
        log.debug("Fetching patients for professional: {}, search: {}, sort: {}", professionalId, search, sort);
        String term = search == null ? "" : search.trim();
        Pageable pageable = PageRequest.of(page, size);
        return "name".equals(sort)
                ? appointmentRepository.findProfessionalPatientsByName(professionalId, term, pageable)
                : appointmentRepository.findProfessionalPatientsByRecent(professionalId, term, pageable);
    }

    @Transactional
    public AppointmentResponseDTO rateAppointment(String appointmentId, String userId, RateAppointmentDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Only completed appointments can be rated");
        }

        PatientProfile patient = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("You can only rate your own appointments");
        }

        if (appointment.getRating() != null) {
            throw new BadRequestException("This appointment has already been rated");
        }

        appointment.setRating(dto.getStars());
        appointment.setRatingComment(dto.getComment());
        Appointment updated = appointmentRepository.save(appointment);
        return toResponseDTO(updated);
    }

    @Transactional
    public void deleteAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        appointmentRepository.delete(appointment);
    }

    private AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient().getUser().getName())
                .patientId(appointment.getPatient().getId())
                .professionalName(appointment.getProfessional().getUser().getName())
                .professionalId(appointment.getProfessional().getId())
                .specialty(appointment.getProfessional().getSpecialty())
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
                .paymentTiming(appointment.getProfessional().getPaymentTiming())
                .rating(appointment.getRating())
                .ratingComment(appointment.getRatingComment())
                .serviceId(appointment.getService() != null ? appointment.getService().getId() : null)
                .serviceName(appointment.getService() != null ? appointment.getService().getName() : null)
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
