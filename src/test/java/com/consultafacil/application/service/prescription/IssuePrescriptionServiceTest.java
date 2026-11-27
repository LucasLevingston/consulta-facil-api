package com.consultafacil.application.service.prescription;

import com.consultafacil.api.dto.prescription.IssuePrescriptionRequest;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.Prescription;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.exception.InvalidStateException;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssuePrescriptionServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock PrescriptionRepositoryPort prescriptionRepository;
    @Mock PrescriptionPdfGenerator pdfGenerator;
    @Mock StoragePort storagePort;
    @Mock EmailPort emailPort;

    @InjectMocks IssuePrescriptionService service;

    private static final String PROF_USER_ID = "user-prof-1";
    private static final String PROF_ID = "prof-1";
    private static final String PATIENT_ID = "patient-1";
    private static final String APPT_ID = "appt-1";

    private ProfessionalProfile professional() {
        User profUser = User.builder().id(PROF_USER_ID).name("Dr. Silva").email("silva@test.com").build();
        return ProfessionalProfile.builder()
                .id(PROF_ID)
                .user(profUser)
                .specialty(Specialty.CLINICA_GERAL)
                .licenseNumber("CRM-12345")
                .build();
    }

    private Appointment appointment(String professionalId, LocalDateTime scheduledAt) {
        User patientUser = User.builder().id("user-p-1").name("João").email("joao@test.com").build();
        PatientProfile patient = PatientProfile.builder().id(PATIENT_ID).user(patientUser).build();
        ProfessionalProfile prof = ProfessionalProfile.builder().id(professionalId).build();
        return Appointment.builder()
                .id(APPT_ID)
                .professional(prof)
                .patient(patient)
                .scheduledAt(scheduledAt)
                .build();
    }

    @Test
    void issue_happyPath_savesPrescriptionAndSendsEmail() {
        ProfessionalProfile prof = professional();
        Appointment appt = appointment(PROF_ID, LocalDateTime.now().minusHours(1));
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(APPT_ID, "Amoxicilina 500mg", "Tomar com água", null);

        when(professionalRepository.findByUserId(PROF_USER_ID)).thenReturn(Optional.of(prof));
        when(appointmentRepository.findById(APPT_ID)).thenReturn(Optional.of(appt));
        when(pdfGenerator.generate(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new byte[]{1, 2, 3});
        when(storagePort.upload(any(), any(), any(), any())).thenReturn("https://s3.example.com/rx.pdf");
        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        when(prescriptionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        service.execute(PROF_USER_ID, req);

        Prescription saved = captor.getValue();
        assertThat(saved.getPdfUrl()).isEqualTo("https://s3.example.com/rx.pdf");
        assertThat(saved.getMedications()).isEqualTo("Amoxicilina 500mg");
        verify(emailPort).sendPrescriptionAvailable(anyString(), anyString(), anyString());
    }

    @Test
    void issue_appointmentBelongsToOtherProfessional_throwsDomainException() {
        ProfessionalProfile prof = professional();
        Appointment appt = appointment("other-prof", LocalDateTime.now().minusHours(1));
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(APPT_ID, "meds", null, null);

        when(professionalRepository.findByUserId(PROF_USER_ID)).thenReturn(Optional.of(prof));
        when(appointmentRepository.findById(APPT_ID)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.execute(PROF_USER_ID, req))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("PRESCRIPTION_FORBIDDEN");

        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void issue_appointmentOlderThan48h_throwsInvalidStateException() {
        ProfessionalProfile prof = professional();
        Appointment appt = appointment(PROF_ID, LocalDateTime.now().minusHours(49));
        IssuePrescriptionRequest req = new IssuePrescriptionRequest(APPT_ID, "meds", null, null);

        when(professionalRepository.findByUserId(PROF_USER_ID)).thenReturn(Optional.of(prof));
        when(appointmentRepository.findById(APPT_ID)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.execute(PROF_USER_ID, req))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("PRESCRIPTION_WINDOW_EXPIRED");
    }

    @Test
    void issue_appointmentNotFound_throwsResourceNotFoundException() {
        when(professionalRepository.findByUserId(PROF_USER_ID)).thenReturn(Optional.of(professional()));
        when(appointmentRepository.findById(APPT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(PROF_USER_ID,
                new IssuePrescriptionRequest(APPT_ID, "meds", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
