package com.consultafacil.application.scheduler;

import com.consultafacil.application.port.in.user.AnonymizeUserUseCase;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataRetentionSchedulerTest {

    @Mock UserRepositoryPort userRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock AnonymizeUserUseCase anonymizeUser;

    DataRetentionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DataRetentionScheduler(userRepository, patientProfileRepository,
                appointmentRepository, anonymizeUser);
    }

    private User userWithId(String id) {
        return User.builder().id(id).name("User " + id).email(id + "@test.com").build();
    }

    @Test
    void anonymizeInactiveUsers_userWithNoProfile_anonymizeCalled() {
        User user = userWithId("u-1");
        when(userRepository.findByCreatedAtBeforeAndDeletedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(List.of(user));
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.empty());

        scheduler.anonymizeInactiveUsers();

        verify(anonymizeUser).execute("u-1");
    }

    @Test
    void anonymizeInactiveUsers_userWithNoActiveAppointments_anonymizeCalled() {
        User user = userWithId("u-2");
        PatientProfile profile = PatientProfile.builder().id("p-2").user(user).build();
        when(userRepository.findByCreatedAtBeforeAndDeletedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(List.of(user));
        when(patientProfileRepository.findByUserId("u-2")).thenReturn(Optional.of(profile));
        when(appointmentRepository.findByPatientIdAndStatus(eq("p-2"), any(AppointmentStatus.class)))
                .thenReturn(List.of());

        scheduler.anonymizeInactiveUsers();

        verify(anonymizeUser).execute("u-2");
    }

    @Test
    void anonymizeInactiveUsers_userWithActiveAppointment_anonymizeNotCalled() {
        User user = userWithId("u-3");
        PatientProfile profile = PatientProfile.builder().id("p-3").user(user).build();
        when(userRepository.findByCreatedAtBeforeAndDeletedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(List.of(user));
        when(patientProfileRepository.findByUserId("u-3")).thenReturn(Optional.of(profile));
        when(appointmentRepository.findByPatientIdAndStatus(eq("p-3"), eq(AppointmentStatus.PENDING)))
                .thenReturn(List.of(Appointment.builder().id("appt-1").build()));
        when(appointmentRepository.findByPatientIdAndStatus(eq("p-3"), eq(AppointmentStatus.CONFIRMED)))
                .thenReturn(List.of());
        when(appointmentRepository.findByPatientIdAndStatus(eq("p-3"), eq(AppointmentStatus.CHECKED_IN)))
                .thenReturn(List.of());
        when(appointmentRepository.findByPatientIdAndStatus(eq("p-3"), eq(AppointmentStatus.IN_PROGRESS)))
                .thenReturn(List.of());

        scheduler.anonymizeInactiveUsers();

        verify(anonymizeUser, never()).execute(anyString());
    }

    @Test
    void anonymizeInactiveUsers_emptyUserList_anonymizeNeverCalled() {
        when(userRepository.findByCreatedAtBeforeAndDeletedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.anonymizeInactiveUsers();

        verify(anonymizeUser, never()).execute(anyString());
    }

    @Test
    void anonymizeInactiveUsers_exceptionDuringOneUser_continuesNext() {
        User first = userWithId("u-4");
        User second = userWithId("u-5");
        when(userRepository.findByCreatedAtBeforeAndDeletedAtIsNull(any(LocalDateTime.class)))
                .thenReturn(List.of(first, second));
        when(patientProfileRepository.findByUserId(anyString())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("anonymize failed")).when(anonymizeUser).execute("u-4");

        scheduler.anonymizeInactiveUsers();

        verify(anonymizeUser).execute("u-4");
        verify(anonymizeUser).execute("u-5");
    }
}
