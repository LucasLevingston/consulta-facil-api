package com.example.consulta.application.service;

import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.*;
import com.example.consulta.domain.enums.ExamRequestStatus;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.port.out.ExamRequestRepositoryPort;
import com.example.consulta.domain.port.out.PatientProfileRepositoryPort;
import com.example.consulta.domain.port.out.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadExamServiceTest {

    @Mock ExamRequestRepositoryPort examRequestRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock StoragePort storagePort;

    @InjectMocks UploadExamService service;

    User user;
    PatientProfile patientProfile;
    Appointment appointment;
    ProfessionalProfile professional;
    ExamRequest examRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id("u-1").email("p@email.com").name("João").password("x").role(UserRole.PATIENT).build();
        patientProfile = new PatientProfile();
        patientProfile.setId("pp-1");
        patientProfile.setUser(user);

        User profUser = User.builder().id("u-2").email("dr@email.com").name("Dra. Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("prof-1");
        professional.setUser(profUser);

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setPatient(patientProfile);
        appointment.setProfessional(professional);

        examRequest = ExamRequest.builder()
                .id("exam-1")
                .appointment(appointment)
                .professional(professional)
                .patient(patientProfile)
                .examName("Hemograma")
                .status(ExamRequestStatus.PENDING)
                .build();

        lenient().when(examRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_validUpload_setsStatusUploaded() throws Exception {
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patientProfile));
        when(storagePort.upload(any(), anyString(), anyString(), anyString())).thenReturn("https://s3/exam.pdf");

        var file = new MockMultipartFile("file", "exam.pdf", "application/pdf", "content".getBytes());
        var result = service.execute("exam-1", "u-1", file);

        assertThat(result.getStatus()).isEqualTo(ExamRequestStatus.UPLOADED);
        assertThat(result.getFileUrl()).isEqualTo("https://s3/exam.pdf");
    }

    @Test
    void execute_examNotFound_throwsNotFound() {
        when(examRequestRepository.findById("bad-id")).thenReturn(Optional.empty());
        var file = new MockMultipartFile("file", "exam.pdf", "application/pdf", "c".getBytes());

        assertThatThrownBy(() -> service.execute("bad-id", "u-1", file))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_patientNotFound_throwsNotFound() {
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));
        when(patientProfileRepository.findByUserId("bad-user")).thenReturn(Optional.empty());
        var file = new MockMultipartFile("file", "exam.pdf", "application/pdf", "c".getBytes());

        assertThatThrownBy(() -> service.execute("exam-1", "bad-user", file))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_wrongPatient_throwsBadRequest() {
        PatientProfile otherPatient = new PatientProfile();
        otherPatient.setId("pp-other");
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));
        when(patientProfileRepository.findByUserId("other-user")).thenReturn(Optional.of(otherPatient));
        var file = new MockMultipartFile("file", "exam.pdf", "application/pdf", "c".getBytes());

        assertThatThrownBy(() -> service.execute("exam-1", "other-user", file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own exam requests");
    }

    @Test
    void execute_alreadyReviewed_throwsBadRequest() {
        examRequest.setStatus(ExamRequestStatus.REVIEWED);
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patientProfile));
        var file = new MockMultipartFile("file", "exam.pdf", "application/pdf", "c".getBytes());

        assertThatThrownBy(() -> service.execute("exam-1", "u-1", file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("reviewed");
    }

    @Test
    void execute_validUpload_callsStorageWithCorrectFolder() throws Exception {
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patientProfile));
        when(storagePort.upload(any(), anyString(), anyString(), anyString())).thenReturn("https://s3/exam.pdf");

        var file = new MockMultipartFile("file", "exam.pdf", "application/pdf", "c".getBytes());
        service.execute("exam-1", "u-1", file);

        verify(storagePort).upload(any(), anyString(), anyString(), org.mockito.ArgumentMatchers.eq("exams"));
    }
}
