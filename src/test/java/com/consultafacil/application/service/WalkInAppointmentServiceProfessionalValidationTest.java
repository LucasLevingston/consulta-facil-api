package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.api.dto.appointment.WalkInAppointmentResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class WalkInAppointmentServiceProfessionalValidationTest extends WalkInAppointmentServiceTestBase {

    @Test
    void create_freeConsultation_noAmountRequired() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        Appointment freeAppt = Appointment.builder().id("appt-free").patient(patient)
                .professional(professional).scheduledAt(LocalDateTime.now().minusHours(1))
                .status(AppointmentStatus.COMPLETED).source(AppointmentSource.WALK_IN)
                .paymentStatus(AppointmentPaymentStatus.FREE).build();
        when(appointmentRepository.save(any())).thenReturn(freeAppt);

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setPaymentStatus(AppointmentPaymentStatus.FREE);
        dto.setPaymentAmount(null);

        WalkInAppointmentResponseDTO result = service.create("adm-1", dto);
        assertThat(result.getPaymentStatus()).isEqualTo(AppointmentPaymentStatus.FREE);
    }

    @Test
    void create_professionalNotActive_throwsIllegalArgument() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        ProfessionalProfile pending = ProfessionalProfile.builder().id("prof-pending")
                .user(professionalUser).status(ProfessionalProfileStatus.PENDING_REVIEW).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-pending")).thenReturn(Optional.of(pending));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setProfessionalId("prof-pending");

        assertThatThrownBy(() -> service.create("adm-1", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void create_professionalNotFound_throwsNotFound() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create("adm-1", buildDTO("patprof-1", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
