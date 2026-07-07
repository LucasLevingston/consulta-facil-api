package com.consultafacil.application.service.appointment;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAppointmentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @InjectMocks DeleteAppointmentService service;

    @Test
    void delete_existingAppointment_callsDelete() {
        Appointment appointment = new Appointment();
        appointment.setId("appt-1");
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        doNothing().when(appointmentRepository).delete(appointment);

        service.delete("appt-1");

        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
