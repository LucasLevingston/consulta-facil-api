package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
abstract class WalkInAppointmentServiceTestBase {

    @Mock protected AppointmentRepositoryPort appointmentRepository;
    @Mock protected ClinicalNoteRepositoryPort clinicalNoteRepository;
    @Mock protected UserRepositoryPort userRepository;
    @Mock protected PatientProfileRepositoryPort patientProfileRepository;
    @Mock protected ProfessionalProfileRepositoryPort professionalProfileRepository;

    @InjectMocks protected WalkInAppointmentService service;

    protected User professionalUser;
    protected ProfessionalProfile professional;
    protected User patientUser;
    protected PatientProfile patient;
    protected Appointment savedAppointment;

    @BeforeEach
    void baseSetUp() {
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

    protected CreateWalkInAppointmentDTO buildDTO(String patientId, String patientName, String patientCpf) {
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
