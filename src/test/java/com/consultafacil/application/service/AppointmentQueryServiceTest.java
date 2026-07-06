package com.consultafacil.application.service;

import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentQueryServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock OwnershipValidator ownershipValidator;

    AppointmentQueryService service;
    ProfessionalProfile professional;
    Appointment appointment;

    @BeforeEach
    void setUp() {
        User profUser = User.builder().id("u-2").email("dr@email.com").name("Dra. Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("prof-1");
        professional.setUser(profUser);
        professional.setSpecialty(Specialty.CARDIOLOGIA);

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setProfessional(professional);
        appointment.setScheduledAt(LocalDateTime.now().plusDays(5));

        service = new AppointmentQueryService(appointmentRepository, patientProfileRepository,
                professionalProfileRepository, ownershipValidator, new AppointmentMapper(),
                new RatingDistributionBuilder());
    }

    @Test
    void getProfessionalAppointments_existingProfessional_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> page = new PageImpl<>(List.of(appointment), pageable, 1);
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(appointmentRepository.findByProfessionalId("prof-1", pageable)).thenReturn(page);

        var result = service.getProfessionalAppointments("prof-1", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProfessionalId()).isEqualTo("prof-1");
    }

    @Test
    void getProfessionalAppointments_professionalNotFound_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(professionalProfileRepository.findById("bad")).thenReturn(Optional.empty());

        var result = service.getProfessionalAppointments("bad", pageable);

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getPageable()).isEqualTo(pageable);
    }

    @Test
    void getProfessionalAppointmentsBySource_existingProfessional_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        appointment.setSource(AppointmentSource.ONLINE);
        Page<Appointment> page = new PageImpl<>(List.of(appointment), pageable, 1);
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(appointmentRepository.findByProfessionalIdAndSource("prof-1", AppointmentSource.ONLINE, pageable))
                .thenReturn(page);

        var result = service.getProfessionalAppointmentsBySource("prof-1", AppointmentSource.ONLINE, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProfessionalId()).isEqualTo("prof-1");
    }

    @Test
    void getProfessionalAppointmentsBySource_professionalNotFound_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(professionalProfileRepository.findById("bad")).thenReturn(Optional.empty());

        var result = service.getProfessionalAppointmentsBySource("bad", AppointmentSource.ONLINE, pageable);

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getPageable()).isEqualTo(pageable);
    }
}
