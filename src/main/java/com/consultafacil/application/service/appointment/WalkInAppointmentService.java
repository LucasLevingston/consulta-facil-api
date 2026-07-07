package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.api.dto.appointment.WalkInAppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.WalkInClinicalNoteDTO;
import com.consultafacil.application.port.in.WalkInAppointmentUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ClinicalNote;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.ClinicalNoteRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalkInAppointmentService implements WalkInAppointmentUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final ClinicalNoteRepositoryPort clinicalNoteRepository;
    private final UserRepositoryPort userRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @Override
    @Transactional
    public WalkInAppointmentResponseDTO create(String authenticatedUserId, CreateWalkInAppointmentDTO dto) {
        validateRequest(dto);

        User authUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", authenticatedUserId));

        ProfessionalProfile professional = professionalProfileRepository.findById(dto.getProfessionalId())
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", dto.getProfessionalId()));

        if (professional.getStatus() != ProfessionalProfileStatus.ACTIVE) {
            throw new IllegalArgumentException("Professional is not active");
        }

        checkAuthorization(authUser, professional);

        PatientProfile patient = resolvePatient(dto);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .professional(professional)
                .scheduledAt(dto.getPerformedAt())
                .reason(dto.getReason())
                .notes(dto.getNotes())
                .status(AppointmentStatus.COMPLETED)
                .source(AppointmentSource.WALK_IN)
                .paymentStatus(dto.getPaymentStatus())
                .paymentAmount(dto.getPaymentAmount())
                .walkInPaymentMethod(dto.getPaymentMethod())
                .durationMinutes(dto.getDurationMinutes())
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("[WalkIn] Appointment created id={} patient={} professional={}",
                appointment.getId(), patient.getId(), professional.getId());

        String clinicalNoteId = null;
        if (dto.getClinicalNote() != null) {
            clinicalNoteId = createClinicalNote(appointment, dto.getClinicalNote()).getId();
        }

        return toDTO(appointment, patient, professional, clinicalNoteId);
    }

    // ── Validation ────────────────────────────────────────────────────────

    private void validateRequest(CreateWalkInAppointmentDTO dto) {
        if (dto.getPerformedAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("performedAt cannot be in the future");
        }

        boolean hasPatientId = dto.getPatientId() != null && !dto.getPatientId().isBlank();
        boolean hasNameAndCpf = dto.getPatientName() != null && !dto.getPatientName().isBlank()
                && dto.getPatientCpf() != null && !dto.getPatientCpf().isBlank();

        if (!hasPatientId && !hasNameAndCpf) {
            throw new IllegalArgumentException("Either patientId or (patientName + patientCpf) is required");
        }

        if (dto.getPaymentStatus() != AppointmentPaymentStatus.FREE && dto.getPaymentAmount() == null) {
            throw new IllegalArgumentException("paymentAmount is required when paymentStatus is not FREE");
        }
    }

    private void checkAuthorization(User authUser, ProfessionalProfile targetProfessional) {
        if (authUser.getRole() == UserRole.ADMIN) return;

        if (authUser.getRole() == UserRole.RECEPTIONIST) return;

        if (authUser.getRole() == UserRole.PROFESSIONAL) {
            ProfessionalProfile ownProfile = professionalProfileRepository.findByUserId(authUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", authUser.getId()));
            if (!ownProfile.getId().equals(targetProfessional.getId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Professional can only register walk-in appointments for themselves");
            }
            return;
        }

        throw new org.springframework.security.access.AccessDeniedException("Insufficient role for walk-in registration");
    }

    // ── Patient resolution ────────────────────────────────────────────────

    private PatientProfile resolvePatient(CreateWalkInAppointmentDTO dto) {
        if (dto.getPatientId() != null && !dto.getPatientId().isBlank()) {
            return patientProfileRepository.findById(dto.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", dto.getPatientId()));
        }

        String normalizedCpf = dto.getPatientCpf().replaceAll("[.\\-]", "");

        return userRepository.findByCpf(normalizedCpf)
                .flatMap(existing -> patientProfileRepository.findByUserId(existing.getId()))
                .orElseGet(() -> createMinimalPatient(dto.getPatientName(), normalizedCpf, dto.getPatientPhone()));
    }

    private PatientProfile createMinimalPatient(String name, String cpf, String phone) {
        String email = "walkin-" + cpf + "@consultafacil.local";

        User user = userRepository.save(User.builder()
                .name(name)
                .email(email)
                .cpf(cpf)
                .phone(phone)
                .role(UserRole.PATIENT)
                .build());

        PatientProfile profile = patientProfileRepository.save(
                PatientProfile.builder().user(user).build());

        log.info("[WalkIn] Created minimal patient userId={} cpf={}", user.getId(), cpf);
        return profile;
    }

    // ── Clinical note ─────────────────────────────────────────────────────

    private ClinicalNote createClinicalNote(Appointment appointment, WalkInClinicalNoteDTO dto) {
        return clinicalNoteRepository.save(ClinicalNote.builder()
                .appointment(appointment)
                .clinicalNotes(dto.getClinicalNotes())
                .diagnosis(dto.getDiagnosis())
                .diagnosisCid(dto.getDiagnosisCid())
                .prescription(dto.getPrescription())
                .treatmentPlan(dto.getTreatmentPlan())
                .followUpInstructions(dto.getFollowUpInstructions())
                .build());
    }

    // ── Mapping ───────────────────────────────────────────────────────────

    private WalkInAppointmentResponseDTO toDTO(Appointment a, PatientProfile patient,
                                               ProfessionalProfile professional, String clinicalNoteId) {
        return WalkInAppointmentResponseDTO.builder()
                .id(a.getId())
                .patientId(patient.getId())
                .patientName(patient.getUser().getName())
                .professionalId(professional.getId())
                .professionalName(professional.getUser().getName())
                .performedAt(a.getScheduledAt())
                .durationMinutes(a.getDurationMinutes())
                .reason(a.getReason())
                .notes(a.getNotes())
                .status(a.getStatus())
                .source(a.getSource())
                .paymentStatus(a.getPaymentStatus())
                .paymentAmount(a.getPaymentAmount())
                .paymentMethod(a.getWalkInPaymentMethod())
                .clinicalNoteId(clinicalNoteId)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
