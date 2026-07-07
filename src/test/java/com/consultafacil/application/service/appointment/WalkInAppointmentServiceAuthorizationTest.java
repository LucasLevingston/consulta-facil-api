package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalkInAppointmentServiceAuthorizationTest extends WalkInAppointmentServiceTestBase {

    @Test
    void create_professional_forOwnProfile_succeeds() {
        when(userRepository.findById("pro-1")).thenReturn(Optional.of(professionalUser));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(professionalProfileRepository.findByUserId("pro-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        service.create("pro-1", buildDTO("patprof-1", null, null));

        verify(appointmentRepository).save(any());
    }

    @Test
    void create_professional_forOtherProfessional_throwsAccessDenied() {
        ProfessionalProfile otherProfessional = ProfessionalProfile.builder().id("other-prof")
                .user(professionalUser).status(ProfessionalProfileStatus.ACTIVE).build();
        ProfessionalProfile ownProfile = ProfessionalProfile.builder().id("own-prof")
                .user(professionalUser).status(ProfessionalProfileStatus.ACTIVE).build();

        when(userRepository.findById("pro-1")).thenReturn(Optional.of(professionalUser));
        when(professionalProfileRepository.findById("other-prof")).thenReturn(Optional.of(otherProfessional));
        when(professionalProfileRepository.findByUserId("pro-1")).thenReturn(Optional.of(ownProfile));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        dto.setProfessionalId("other-prof");

        assertThatThrownBy(() -> service.create("pro-1", dto))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void create_receptionist_anyProfessional_succeeds() {
        User receptionist = User.builder().id("rec-1").role(UserRole.RECEPTIONIST).build();
        when(userRepository.findById("rec-1")).thenReturn(Optional.of(receptionist));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        service.create("rec-1", buildDTO("patprof-1", null, null));

        verify(appointmentRepository).save(any());
    }
}
