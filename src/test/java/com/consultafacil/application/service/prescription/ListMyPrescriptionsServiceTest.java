package com.consultafacil.application.service.prescription;

import com.consultafacil.api.dto.prescription.PrescriptionDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.Prescription;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListMyPrescriptionsServiceTest {

    @Mock PatientProfileRepositoryPort patientRepository;
    @Mock PrescriptionRepositoryPort prescriptionRepository;

    @InjectMocks ListMyPrescriptionsService service;

    @Test
    void listMine_returnsMappedPage() {
        String patientUserId = "user-pat-1";
        String patientId = "pat-1";
        PatientProfile patient = PatientProfile.builder().id(patientId).build();
        when(patientRepository.findByUserId(patientUserId)).thenReturn(Optional.of(patient));

        User profUser = User.builder().id("u-p").name("Dr. Ana").build();
        Prescription rx = Prescription.builder()
                .id("rx-1")
                .appointment(Appointment.builder().id("appt-1").build())
                .professional(ProfessionalProfile.builder().id("prof-1").user(profUser).build())
                .patient(patient)
                .medications("Paracetamol 500mg")
                .build();
        Page<Prescription> page = new PageImpl<>(List.of(rx));
        when(prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(eq(patientId), eq(Pageable.unpaged())))
                .thenReturn(page);

        Page<PrescriptionDTO> result = service.execute(patientUserId, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).medications()).isEqualTo("Paracetamol 500mg");
    }

    @Test
    void listMine_patientProfileNotFound_throwsResourceNotFoundException() {
        when(patientRepository.findByUserId("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad", Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
