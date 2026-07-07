package com.consultafacil.application.service.examrequest;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.consultafacil.domain.enums.ExamType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import com.consultafacil.domain.enums.ExamType;

@ExtendWith(MockitoExtension.class)
class GetMyExamsServiceTest {

    @Mock ExamRequestRepositoryPort examRequestRepository;
    @Mock UserRepositoryPort userRepository;

    @InjectMocks GetMyExamsService service;

    User patientUser;
    User professionalUser;
    PatientProfile patientProfile;
    ProfessionalProfile professionalProfile;
    Appointment appointment;
    ExamRequest examRequest;

    @BeforeEach
    void setUp() {
        patientUser = User.builder().id("u-patient").email("p@email.com").name("João")
                .password("x").role(UserRole.PATIENT).build();

        professionalUser = User.builder().id("u-prof").email("dr@email.com").name("Dra. Ana")
                .password("x").role(UserRole.PROFESSIONAL).build();

        patientProfile = new PatientProfile();
        patientProfile.setId("pp-1");
        patientProfile.setUser(patientUser);

        professionalProfile = new ProfessionalProfile();
        professionalProfile.setId("prof-1");
        professionalProfile.setUser(professionalUser);

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setPatient(patientProfile);
        appointment.setProfessional(professionalProfile);

        examRequest = ExamRequest.builder()
                .id("exam-1")
                .appointment(appointment)
                .professional(professionalProfile)
                .patient(patientProfile)
                .examName(ExamType.HEMOGRAMA_COMPLETO)
                .status(ExamRequestStatus.PENDING)
                .build();
    }

    @Test
    void execute_asPatient_noFilter_returnsPatientExams() {
        when(userRepository.findById("u-patient")).thenReturn(Optional.of(patientUser));
        when(examRequestRepository.findByPatientUserId("u-patient")).thenReturn(List.of(examRequest));

        var result = service.execute("u-patient", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExamName()).isEqualTo(ExamType.HEMOGRAMA_COMPLETO);
        verify(examRequestRepository).findByPatientUserId("u-patient");
        verify(examRequestRepository, never()).findByProfessionalUserId(any());
    }

    @Test
    void execute_asPatient_withStatusFilter_returnsFilteredExams() {
        when(userRepository.findById("u-patient")).thenReturn(Optional.of(patientUser));
        when(examRequestRepository.findByPatientUserIdAndStatus("u-patient", ExamRequestStatus.PENDING))
                .thenReturn(List.of(examRequest));

        var result = service.execute("u-patient", ExamRequestStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ExamRequestStatus.PENDING);
        verify(examRequestRepository).findByPatientUserIdAndStatus("u-patient", ExamRequestStatus.PENDING);
    }

    @Test
    void execute_asProfessional_noFilter_returnsProfessionalExams() {
        when(userRepository.findById("u-prof")).thenReturn(Optional.of(professionalUser));
        when(examRequestRepository.findByProfessionalUserId("u-prof")).thenReturn(List.of(examRequest));

        var result = service.execute("u-prof", null);

        assertThat(result).hasSize(1);
        verify(examRequestRepository).findByProfessionalUserId("u-prof");
        verify(examRequestRepository, never()).findByPatientUserId(any());
    }

    @Test
    void execute_asProfessional_withStatusFilter_returnsFilteredExams() {
        when(userRepository.findById("u-prof")).thenReturn(Optional.of(professionalUser));
        when(examRequestRepository.findByProfessionalUserIdAndStatus("u-prof", ExamRequestStatus.UPLOADED))
                .thenReturn(List.of(examRequest));

        var result = service.execute("u-prof", ExamRequestStatus.UPLOADED);

        assertThat(result).hasSize(1);
        verify(examRequestRepository).findByProfessionalUserIdAndStatus("u-prof", ExamRequestStatus.UPLOADED);
    }

    @Test
    void execute_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad-id", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_noExams_returnsEmptyList() {
        when(userRepository.findById("u-patient")).thenReturn(Optional.of(patientUser));
        when(examRequestRepository.findByPatientUserId("u-patient")).thenReturn(List.of());

        var result = service.execute("u-patient", null);

        assertThat(result).isEmpty();
    }

    @Test
    void execute_mapsAllDtoFields() {
        examRequest.setFileUrl("https://s3/exam.pdf");
        examRequest.setFileName("exam.pdf");
        examRequest.setProfessionalNotes("Resultado normal");

        when(userRepository.findById("u-patient")).thenReturn(Optional.of(patientUser));
        when(examRequestRepository.findByPatientUserId("u-patient")).thenReturn(List.of(examRequest));

        var result = service.execute("u-patient", null);
        var dto = result.get(0);

        assertThat(dto.getId()).isEqualTo("exam-1");
        assertThat(dto.getAppointmentId()).isEqualTo("appt-1");
        assertThat(dto.getProfessionalId()).isEqualTo("prof-1");
        assertThat(dto.getProfessionalName()).isEqualTo("Dra. Ana");
        assertThat(dto.getPatientId()).isEqualTo("pp-1");
        assertThat(dto.getPatientName()).isEqualTo("João");
        assertThat(dto.getFileUrl()).isEqualTo("https://s3/exam.pdf");
        assertThat(dto.getProfessionalNotes()).isEqualTo("Resultado normal");
    }
}
