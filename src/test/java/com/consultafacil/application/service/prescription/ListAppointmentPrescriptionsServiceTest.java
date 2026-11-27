package com.consultafacil.application.service.prescription;

import com.consultafacil.api.dto.prescription.PrescriptionDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.Prescription;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAppointmentPrescriptionsServiceTest {

    @Mock PrescriptionRepositoryPort prescriptionRepository;

    @InjectMocks ListAppointmentPrescriptionsService service;

    @Test
    void list_returnsMappedDTOs() {
        User profUser = User.builder().id("u-p").name("Dr. Ana").email("ana@test.com").build();
        User patUser = User.builder().id("u-pat").name("João").email("joao@test.com").build();
        Appointment appt = Appointment.builder().id("appt-1").build();
        Prescription rx = Prescription.builder()
                .id("rx-1")
                .appointment(appt)
                .professional(ProfessionalProfile.builder().id("prof-1").user(profUser).build())
                .patient(PatientProfile.builder().id("pat-1").user(patUser).build())
                .medications("Ibuprofeno 400mg")
                .build();
        when(prescriptionRepository.findByAppointmentIdOrderByCreatedAtDesc("appt-1")).thenReturn(List.of(rx));

        List<PrescriptionDTO> result = service.execute("user-1", "appt-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).medications()).isEqualTo("Ibuprofeno 400mg");
        assertThat(result.get(0).professionalName()).isEqualTo("Dr. Ana");
    }

    @Test
    void list_noResults_returnsEmptyList() {
        when(prescriptionRepository.findByAppointmentIdOrderByCreatedAtDesc("appt-x")).thenReturn(List.of());

        List<PrescriptionDTO> result = service.execute("user-1", "appt-x");

        assertThat(result).isEmpty();
    }
}
