package com.consultafacil.adapter.out.notification;

import com.consultafacil.core.messaging.EventPublisher;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.event.AppointmentCanceledEvent;
import com.consultafacil.domain.event.AppointmentConfirmedEvent;
import com.consultafacil.domain.event.AppointmentCreatedEvent;
import com.consultafacil.domain.port.out.notification.NotificationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentNotificationAdapterTest {

    @Mock NotificationRepositoryPort notificationRepository;
    @Mock EventPublisher eventPublisher;

    AppointmentNotificationAdapter adapter;
    User patientUser;
    User professionalUser;
    Appointment appointment;

    @BeforeEach
    void setUp() {
        adapter = new AppointmentNotificationAdapter(notificationRepository, eventPublisher);
        patientUser = User.builder().id("pu").name("João").email("joao@example.com").phone("+5511911110000").build();
        professionalUser = User.builder().id("du").name("Dra. Ana").email("ana@example.com").phone("+5511922220000").build();
        PatientProfile patient = PatientProfile.builder().id("p1").user(patientUser).build();
        ProfessionalProfile professional = ProfessionalProfile.builder().id("d1").user(professionalUser).build();
        appointment = Appointment.builder()
                .id("a1").patient(patient).professional(professional)
                .scheduledAt(LocalDateTime.of(2026, 12, 1, 14, 30))
                .modality(AppointmentModality.ONLINE)
                .build();
    }

    @Test
    void scheduled_sendsBothPhonesWhenEveryoneAcceptsWhatsApp() {
        adapter.notifyScheduled(appointment);

        ArgumentCaptor<AppointmentCreatedEvent> event = ArgumentCaptor.forClass(AppointmentCreatedEvent.class);
        verify(eventPublisher).publishAppointmentCreated(event.capture());
        assertThat(event.getValue().patientPhone()).isEqualTo("+5511911110000");
        assertThat(event.getValue().professionalPhone()).isEqualTo("+5511922220000");
        assertThat(event.getValue().patientEmail()).isEqualTo("joao@example.com");
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void scheduled_omitsThePhoneOfAPatientWhoOptedOut() {
        patientUser.setWhatsappNotificationsEnabled(false);

        adapter.notifyScheduled(appointment);

        ArgumentCaptor<AppointmentCreatedEvent> event = ArgumentCaptor.forClass(AppointmentCreatedEvent.class);
        verify(eventPublisher).publishAppointmentCreated(event.capture());
        assertThat(event.getValue().patientPhone()).isNull();
        assertThat(event.getValue().professionalPhone()).isEqualTo("+5511922220000");
        assertThat(event.getValue().patientEmail()).isEqualTo("joao@example.com");
    }

    @Test
    void confirmed_omitsThePatientPhoneAfterOptOut() {
        patientUser.setWhatsappNotificationsEnabled(false);

        adapter.notifyConfirmed(appointment);

        ArgumentCaptor<AppointmentConfirmedEvent> event = ArgumentCaptor.forClass(AppointmentConfirmedEvent.class);
        verify(eventPublisher).publishAppointmentConfirmed(event.capture());
        assertThat(event.getValue().patientPhone()).isNull();
    }

    @Test
    void canceled_omitsTheProfessionalPhoneAfterOptOut() {
        professionalUser.setWhatsappNotificationsEnabled(false);

        adapter.notifyCanceled(appointment);

        ArgumentCaptor<AppointmentCanceledEvent> event = ArgumentCaptor.forClass(AppointmentCanceledEvent.class);
        verify(eventPublisher).publishAppointmentCanceled(event.capture());
        assertThat(event.getValue().professionalPhone()).isNull();
        assertThat(event.getValue().patientPhone()).isEqualTo("+5511911110000");
    }
}
