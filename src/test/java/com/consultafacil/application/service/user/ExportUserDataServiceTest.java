package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.UserDataExportDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.exception.RateLimitException;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportUserDataServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;

    ExportUserDataService service;

    @BeforeEach
    void setUp() {
        service = new ExportUserDataService(userRepository, patientProfileRepository, appointmentRepository);
    }

    private User buildUser() {
        return User.builder()
                .id("u-1")
                .name("Maria")
                .email("maria@email.com")
                .cpf("123.456.789-00")
                .phone("11999999999")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void export_returnsDtoWithCorrectUserFieldsAndEmptyAppointmentList() {
        User user = buildUser();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.empty());

        UserDataExportDTO dto = service.execute("u-1");

        assertThat(dto.id()).isEqualTo("u-1");
        assertThat(dto.name()).isEqualTo("Maria");
        assertThat(dto.email()).isEqualTo("maria@email.com");
        assertThat(dto.cpf()).isEqualTo("123.456.789-00");
        assertThat(dto.appointments()).isEmpty();
    }

    @Test
    void export_rateLimitExceeded_throwsRateLimitException() {
        User user = buildUser();
        user.setLastDataExportAt(LocalDateTime.now().minusMinutes(30));
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute("u-1"))
                .isInstanceOf(RateLimitException.class)
                .hasMessageContaining("Aguarde 1 hora");
    }

    @Test
    void export_updatesLastDataExportAt() {
        User user = buildUser();
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.empty());

        service.execute("u-1");

        assertThat(user.getLastDataExportAt()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void export_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void export_withPatientProfile_returnsAppointments() {
        User user = buildUser();
        PatientProfile profile = PatientProfile.builder().id("p-1").user(user).build();

        Appointment appointment = Appointment.builder()
                .id("a-1")
                .status(AppointmentStatus.COMPLETED)
                .scheduledAt(LocalDateTime.now().minusDays(1))
                .patient(profile)
                .build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(profile));
        when(appointmentRepository.findByPatientId(eq("p-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(appointment)));

        UserDataExportDTO dto = service.execute("u-1");

        assertThat(dto.appointments()).hasSize(1);
        assertThat(dto.appointments().get(0).id()).isEqualTo("a-1");
        assertThat(dto.appointments().get(0).status()).isEqualTo("COMPLETED");
    }
}
