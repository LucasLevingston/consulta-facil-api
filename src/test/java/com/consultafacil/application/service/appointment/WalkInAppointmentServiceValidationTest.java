package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class WalkInAppointmentServiceValidationTest extends WalkInAppointmentServiceTestBase {

    @Test
    void create_futurePerformedAt_throwsIllegalArgument() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setPerformedAt(LocalDateTime.now().plusHours(1));

        assertThatThrownBy(() -> service.create("adm-1", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void create_noPatientIdentifier_throwsIllegalArgument() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));

        CreateWalkInAppointmentDTO dto = buildDTO(null, null, null);

        assertThatThrownBy(() -> service.create("adm-1", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("patientId");
    }

    @Test
    void create_notFree_missingPaymentAmount_throwsIllegalArgument() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setPaymentAmount(null);
        dto.setPaymentStatus(AppointmentPaymentStatus.PAID);

        assertThatThrownBy(() -> service.create("adm-1", dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentAmount");
    }

}
