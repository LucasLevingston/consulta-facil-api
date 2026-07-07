package com.consultafacil.application.service.examrequest;

import com.consultafacil.api.dto.examscheduling.ScheduleExamDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.ExamSchedulingStatus;
import com.consultafacil.domain.port.out.ExamLabRepositoryPort;
import com.consultafacil.domain.port.out.ExamRequestRepositoryPort;
import com.consultafacil.domain.port.out.ExamSchedulingRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.consultafacil.domain.enums.ExamType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import com.consultafacil.domain.enums.ExamType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.consultafacil.domain.enums.ExamType;

@ExtendWith(MockitoExtension.class)
class ScheduleExamServiceTest {

    @Mock ExamRequestRepositoryPort examRequestRepository;
    @Mock ExamLabRepositoryPort examLabRepository;
    @Mock ExamSchedulingRepositoryPort examSchedulingRepository;

    @InjectMocks ScheduleExamService service;

    User patientUser;
    PatientProfile patientProfile;
    ProfessionalProfile professionalProfile;
    ExamRequest examRequest;
    ExamLab examLab;
    ScheduleExamDTO dto;
    LocalDate date;
    LocalTime time;

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
                .status(ExamRequestStatus.PENDING)
                .build();

        examLab = ExamLab.builder()
                .id("lab-1")
                .name("Lab Saúde")
                .address("Rua A, 100")
                .city("João Pessoa")
                .phone("(83) 9999-0000")
                .status("ACTIVE")
                .build();

        date = LocalDate.of(2026, 7, 1);
        time = LocalTime.of(9, 0);

        dto = new ScheduleExamDTO();
        dto.setExamRequestId("exam-1");
        dto.setExamLabId("lab-1");
        dto.setScheduledDate(date);
        dto.setScheduledTime(time);
    }

    @Test
    void execute_success_createsSchedulingAndUpdatesStatus() {
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(examLab));
        when(examSchedulingRepository.findByExamLabIdAndScheduledDate("lab-1", date)).thenReturn(List.of());
        when(examSchedulingRepository.save(any())).thenAnswer(inv -> {
            ExamScheduling s = inv.getArgument(0);
            s = ExamScheduling.builder()
                    .id("sched-1")
                    .examRequest(s.getExamRequest())
                    .examLab(s.getExamLab())
                    .scheduledDate(s.getScheduledDate())
                    .scheduledTime(s.getScheduledTime())
                    .status(ExamSchedulingStatus.SCHEDULED)
                    .build();
            return s;
        });
        when(examRequestRepository.save(any())).thenReturn(examRequest);

        var result = service.execute("u-patient", dto);

        assertThat(result.getId()).isEqualTo("sched-1");
        assertThat(result.getExamLabName()).isEqualTo("Lab Saúde");
        assertThat(result.getScheduledDate()).isEqualTo(date);
        assertThat(result.getScheduledTime()).isEqualTo(time);
        assertThat(result.getStatus()).isEqualTo(ExamSchedulingStatus.SCHEDULED);
        verify(examRequestRepository).save(argThat(r -> r.getStatus() == ExamRequestStatus.SCHEDULED));
    }

    @Test
    void execute_examRequestNotFound_throwsResourceNotFoundException() {
        when(examRequestRepository.findById("bad-id")).thenReturn(Optional.empty());
        dto.setExamRequestId("bad-id");

        assertThatThrownBy(() -> service.execute("u-patient", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_wrongPatient_throwsBadRequestException() {
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));

        assertThatThrownBy(() -> service.execute("other-user", dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void execute_alreadyScheduled_throwsBadRequestException() {
        examRequest.setStatus(ExamRequestStatus.SCHEDULED);
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));

        assertThatThrownBy(() -> service.execute("u-patient", dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already scheduled");
    }

    @Test
    void execute_slotTaken_throwsBadRequestException() {
        ExamScheduling existing = ExamScheduling.builder()
                .id("sched-existing")
                .examLab(examLab)
                .scheduledDate(date)
                .scheduledTime(time)
                .status(ExamSchedulingStatus.SCHEDULED)
                .build();

        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.of(examLab));
        when(examSchedulingRepository.findByExamLabIdAndScheduledDate("lab-1", date))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.execute("u-patient", dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no longer available");
    }

    @Test
    void execute_examLabNotFound_throwsResourceNotFoundException() {
        when(examRequestRepository.findById("exam-1")).thenReturn(Optional.of(examRequest));
        when(examLabRepository.findById("lab-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("u-patient", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
