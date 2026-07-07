package com.consultafacil.application.service.appointment;

import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class GetProfessionalAppointmentsServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;

    GetProfessionalAppointmentsService service;
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

        service = new GetProfessionalAppointmentsService(professionalProfileRepository, appointmentRepository,
                new AppointmentMapper());
    }

    @Test
    void execute_existingProfessional_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> page = new PageImpl<>(List.of(appointment), pageable, 1);
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(appointmentRepository.findByProfessionalId("prof-1", pageable)).thenReturn(page);

        var result = service.execute("prof-1", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProfessionalId()).isEqualTo("prof-1");
    }

    @Test
    void execute_professionalNotFound_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(professionalProfileRepository.findById("bad")).thenReturn(Optional.empty());

        var result = service.execute("bad", pageable);

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getPageable()).isEqualTo(pageable);
    }
}
