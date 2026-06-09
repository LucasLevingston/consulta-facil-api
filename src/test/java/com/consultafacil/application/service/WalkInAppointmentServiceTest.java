package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.api.dto.appointment.WalkInAppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.WalkInClinicalNoteDTO;
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
import com.consultafacil.domain.enums.WalkInPaymentMethod;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.ClinicalNoteRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WalkInAppointmentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock ClinicalNoteRepositoryPort clinicalNoteRepository;
    @Mock UserRepositoryPort userRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;

    @InjectMocks WalkInAppointmentService service;

    User professionalUser;
    ProfessionalProfile professional;
    User patientUser;
    PatientProfile patient;
    Appointment savedAppointment;

    @BeforeEach
    void setUp() {
        professionalUser = User.builder().id("pro-1").name("Dr. Ana").email("ana@clinic.com")
                .role(UserRole.PROFESSIONAL).build();
        professional = ProfessionalProfile.builder().id("prof-1").user(professionalUser)
                .status(ProfessionalProfileStatus.ACTIVE).build();

        patientUser = User.builder().id("pat-1").name("João Silva").email("joao@email.com")
                .role(UserRole.PATIENT).build();
        patient = PatientProfile.builder().id("patprof-1").user(patientUser).build();

        savedAppointment = Appointment.builder()
                .id("appt-1").patient(patient).professional(professional)
                .scheduledAt(LocalDateTime.now().minusHours(1))
                .status(AppointmentStatus.COMPLETED)
                .source(AppointmentSource.WALK_IN)
                .paymentStatus(AppointmentPaymentStatus.PAID)
                .paymentAmount(new BigDecimal("250.00"))
                .walkInPaymentMethod(WalkInPaymentMethod.PIX)
                .build();

        when(appointmentRepository.save(any())).thenReturn(savedAppointment);
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            return ClinicalNote.builder().id("note-1").appointment(n.getAppointment())
                    .clinicalNotes(n.getClinicalNotes()).diagnosis(n.getDiagnosis()).build();
        });
    }

    // ── Happy path ─────────────────────────────────────────────────────────

    @Test
    void create_withRegisteredPatient_admin_succeeds() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);

        WalkInAppointmentResponseDTO result = service.create("adm-1", dto);

        assertThat(result.getId()).isEqualTo("appt-1");
        assertThat(result.getSource()).isEqualTo(AppointmentSource.WALK_IN);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        verify(appointmentRepository).save(any());
    }

    @Test
    void create_withClinicalNote_savesNoteInSameTransaction() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        WalkInClinicalNoteDTO noteDto = new WalkInClinicalNoteDTO();
        noteDto.setClinicalNotes("Paciente estável");
        noteDto.setDiagnosis("Hipertensão");
        dto.setClinicalNote(noteDto);

        WalkInAppointmentResponseDTO result = service.create("adm-1", dto);

        verify(clinicalNoteRepository).save(any());
        assertThat(result.getClinicalNoteId()).isEqualTo("note-1");
    }

    @Test
    void create_noClinicalNote_doesNotSaveNote() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        WalkInAppointmentResponseDTO result = service.create("adm-1", buildDTO("patprof-1", null, null));

        verifyNoInteractions(clinicalNoteRepository);
        assertThat(result.getClinicalNoteId()).isNull();
    }

    // ── Unregistered patient ───────────────────────────────────────────────

    @Test
    void create_unregisteredPatient_newCpf_createsMinimalPatient() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(userRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(patientUser);
        when(patientProfileRepository.save(any())).thenReturn(patient);

        CreateWalkInAppointmentDTO dto = buildDTO(null, "João da Silva", "123.456.789-01");

        service.create("adm-1", dto);

        verify(userRepository).save(any());
        verify(patientProfileRepository).save(any());
    }

    @Test
    void create_unregisteredPatient_existingCpf_reusesExistingPatient() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(userRepository.findByCpf("12345678901")).thenReturn(Optional.of(patientUser));
        when(patientProfileRepository.findByUserId("pat-1")).thenReturn(Optional.of(patient));

        CreateWalkInAppointmentDTO dto = buildDTO(null, "João da Silva", "123.456.789-01");

        service.create("adm-1", dto);

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_cpfNormalized_removesDotsAndDashes() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(userRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(patientUser);
        when(patientProfileRepository.save(any())).thenReturn(patient);

        CreateWalkInAppointmentDTO dto = buildDTO(null, "Test", "123.456.789-01");
        service.create("adm-1", dto);

        verify(userRepository).findByCpf("12345678901");
    }

    // ── Authorization ──────────────────────────────────────────────────────

    @Test
    void create_professional_forOwnProfile_succeeds() {
        when(userRepository.findById("pro-1")).thenReturn(Optional.of(professionalUser));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(professionalProfileRepository.findByUserId("pro-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        service.create("pro-1", buildDTO("patprof-1", null, null));

        verify(appointmentRepository).save(any());
    }

    @Test
    void create_professional_forOtherProfessional_throwsAccessDenied() {
        ProfessionalProfile otherProfessional = ProfessionalProfile.builder().id("other-prof")
                .user(professionalUser).status(ProfessionalProfileStatus.ACTIVE).build();
        ProfessionalProfile ownProfile = ProfessionalProfile.builder().id("own-prof")
                .user(professionalUser).status(ProfessionalProfileStatus.ACTIVE).build();

        when(userRepository.findById("pro-1")).thenReturn(Optional.of(professionalUser));
        when(professionalProfileRepository.findById("other-prof")).thenReturn(Optional.of(otherProfessional));
        when(professionalProfileRepository.findByUserId("pro-1")).thenReturn(Optional.of(ownProfile));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setProfessionalId("other-prof");

        assertThatThrownBy(() -> service.create("pro-1", dto))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void create_receptionist_anyProfessional_succeeds() {
        User receptionist = User.builder().id("rec-1").role(UserRole.RECEPTIONIST).build();
        when(userRepository.findById("rec-1")).thenReturn(Optional.of(receptionist));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        service.create("rec-1", buildDTO("patprof-1", null, null));

        verify(appointmentRepository).save(any());
    }

    // ── Validation ─────────────────────────────────────────────────────────

    @Test
    void create_futurePerformedAt_throwsIllegalArgument() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setPerformedAt(LocalDateTime.now().plusHours(1));

        assertThatThrownBy(() -> service.create("adm-1", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void create_noPatientIdentifier_throwsIllegalArgument() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));

        CreateWalkInAppointmentDTO dto = buildDTO(null, null, null);

        assertThatThrownBy(() -> service.create("adm-1", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("patientId");
    }

    @Test
    void create_notFree_missingPaymentAmount_throwsIllegalArgument() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setPaymentAmount(null);
        dto.setPaymentStatus(AppointmentPaymentStatus.PAID);

        assertThatThrownBy(() -> service.create("adm-1", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentAmount");
    }

    @Test
    void create_freeConsultation_noAmountRequired() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        Appointment freeAppt = Appointment.builder().id("appt-free").patient(patient)
                .professional(professional).scheduledAt(LocalDateTime.now().minusHours(1))
                .status(AppointmentStatus.COMPLETED).source(AppointmentSource.WALK_IN)
                .paymentStatus(AppointmentPaymentStatus.FREE).build();
        when(appointmentRepository.save(any())).thenReturn(freeAppt);

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setPaymentStatus(AppointmentPaymentStatus.FREE);
        dto.setPaymentAmount(null);

        WalkInAppointmentResponseDTO result = service.create("adm-1", dto);
        assertThat(result.getPaymentStatus()).isEqualTo(AppointmentPaymentStatus.FREE);
    }

    @Test
    void create_professionalNotActive_throwsIllegalArgument() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        ProfessionalProfile pending = ProfessionalProfile.builder().id("prof-pending")
                .user(professionalUser).status(ProfessionalProfileStatus.PENDING_REVIEW).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-pending")).thenReturn(Optional.of(pending));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setProfessionalId("prof-pending");

        assertThatThrownBy(() -> service.create("adm-1", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void create_professionalNotFound_throwsNotFound() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create("adm-1", buildDTO("patprof-1", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private CreateWalkInAppointmentDTO buildDTO(String patientId, String patientName, String patientCpf) {
        CreateWalkInAppointmentDTO dto = new CreateWalkInAppointmentDTO();
        dto.setPatientId(patientId);
        dto.setPatientName(patientName);
        dto.setPatientCpf(patientCpf);
        dto.setProfessionalId("prof-1");
        dto.setPerformedAt(LocalDateTime.now().minusHours(1));
        dto.setPaymentStatus(AppointmentPaymentStatus.PAID);
        dto.setPaymentAmount(new BigDecimal("250.00"));
        dto.setPaymentMethod(WalkInPaymentMethod.PIX);
        return dto;
    }
}
