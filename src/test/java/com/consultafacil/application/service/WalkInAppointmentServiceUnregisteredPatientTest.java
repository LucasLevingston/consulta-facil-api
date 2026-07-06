package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalkInAppointmentServiceUnregisteredPatientTest extends WalkInAppointmentServiceTestBase {

    @Test
    void create_unregisteredPatient_newCpf_createsMinimalPatient() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(userRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(patientUser);
        when(patientProfileRepository.save(any())).thenReturn(patient);

        CreateWalkInAppointmentDTO dto = buildDTO(null, "João da Silva", "123.456.789-01");

        service.create("adm-1", dto);

        verify(userRepository).save(any());
        verify(patientProfileRepository).save(any());
    }

    @Test
    void create_unregisteredPatient_existingCpf_reusesExistingPatient() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(userRepository.findByCpf("12345678901")).thenReturn(Optional.of(patientUser));
        when(patientProfileRepository.findByUserId("pat-1")).thenReturn(Optional.of(patient));

        CreateWalkInAppointmentDTO dto = buildDTO(null, "João da Silva", "123.456.789-01");

        service.create("adm-1", dto);

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_cpfNormalized_removesDotsAndDashes() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(userRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(patientUser);
        when(patientProfileRepository.save(any())).thenReturn(patient);

        CreateWalkInAppointmentDTO dto = buildDTO(null, "Test", "123.456.789-01");
        service.create("adm-1", dto);

        verify(userRepository).findByCpf("12345678901");
    }
}
