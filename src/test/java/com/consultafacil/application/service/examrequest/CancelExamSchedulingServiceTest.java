package com.consultafacil.application.service.examrequest;

import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.ExamSchedulingStatus;
import com.consultafacil.domain.port.out.examrequest.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.examrequest.ExamSchedulingRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.consultafacil.domain.enums.ExamType;

import java.util.Optional;
import com.consultafacil.domain.enums.ExamType;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.consultafacil.domain.enums.ExamType;

@ExtendWith(MockitoExtension.class)
class CancelExamSchedulingServiceTest {

    @Mock ExamSchedulingRepositoryPort examSchedulingRepository;
    @Mock ExamRequestRepositoryPort examRequestRepository;

    @InjectMocks CancelExamSchedulingService service;

    User patientUser;
    PatientProfile patientProfile;
    ProfessionalProfile professionalProfile;
    ExamRequest examRequest;
    ExamScheduling scheduling;

    @BeforeEach
    void setUp() {
        patientUser = User.builder().id("u-patient").name("João").email("p@email.com")
                .password("x").build();

        patientProfile = new PatientProfile();
        patientProfile.setId("pp-1");
        patientProfile.setUser(patientUser);

        professionalProfile = new ProfessionalProfile();
        professionalProfile.setId("prof-1");

        examRequest = ExamRequest.builder()
                .id("exam-1")
                .patient(patientProfile)
                .professional(professionalProfile)
                .examName(ExamType.HEMOGRAMA_COMPLETO)
                .status(ExamRequestStatus.SCHEDULED)
                .build();

        scheduling = ExamScheduling.builder()
                .id("sched-1")
                .examRequest(examRequest)
                .status(ExamSchedulingStatus.SCHEDULED)
                .build();
    }

    @Test
    void execute_success_cancelsAndRevertExamStatus() {
        when(examSchedulingRepository.findById("sched-1")).thenReturn(Optional.of(scheduling));
        when(examSchedulingRepository.save(any())).thenReturn(scheduling);
        when(examRequestRepository.save(any())).thenReturn(examRequest);

        service.execute("sched-1", "u-patient");

        verify(examSchedulingRepository).save(argThat(s -> s.getStatus() == ExamSchedulingStatus.CANCELLED));
        verify(examRequestRepository).save(argThat(r -> r.getStatus() == ExamRequestStatus.PENDING));
    }

    @Test
    void execute_schedulingNotFound_throwsResourceNotFoundException() {
        when(examSchedulingRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad-id", "u-patient"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_wrongPatient_throwsBadRequestException() {
        when(examSchedulingRepository.findById("sched-1")).thenReturn(Optional.of(scheduling));

        assertThatThrownBy(() -> service.execute("sched-1", "other-user"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void execute_alreadyCancelled_throwsBadRequestException() {
        scheduling.setStatus(ExamSchedulingStatus.CANCELLED);
        when(examSchedulingRepository.findById("sched-1")).thenReturn(Optional.of(scheduling));

        assertThatThrownBy(() -> service.execute("sched-1", "u-patient"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already cancelled");
    }
}
