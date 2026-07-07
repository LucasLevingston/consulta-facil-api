package com.consultafacil.application.service.appointment;

import com.consultafacil.application.observability.BusinessMetrics;
import com.consultafacil.core.messaging.EventPublisher;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.*;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HandleAppointmentPaymentWebhookServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock BusinessMetrics businessMetrics;
    @Mock EventPublisher eventPublisher;

    @InjectMocks HandleAppointmentPaymentWebhookService service;

    Appointment appointment;

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@e.com").name("João").password("x").role(UserRole.PATIENT).build();
        PatientProfile patient = new PatientProfile(); patient.setId("pp-1"); patient.setUser(pUser);
        User dUser = User.builder().id("u-2").email("d@e.com").name("Dra.").password("x").role(UserRole.PROFESSIONAL).build();
        ProfessionalProfile prof = new ProfessionalProfile(); prof.setId("pr-1"); prof.setUser(dUser);

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setPatient(patient);
        appointment.setProfessional(prof);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setPaymentAmount(new BigDecimal("250.00"));

        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_nonPaymentType_doesNothing() {
        service.execute(Map.of("type", "preapproval"));
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void execute_missingDataField_doesNothing() {
        service.execute(Map.of("type", "payment"));
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void execute_blankPaymentId_doesNothing() {
        service.execute(Map.of("type", "payment", "data", Map.of("id", "")));
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void execute_exceptionFromPaymentClient_doesNotThrow() {
        assertThatCode(() ->
                service.execute(Map.of("type", "payment", "data", Map.of("id", "12345"))))
                .doesNotThrowAnyException();
    }

    @Test
    void execute_approvedPayment_marksAppointmentPaid() throws Exception {
        Payment mockPayment = mock(Payment.class);
        when(mockPayment.getStatus()).thenReturn("approved");
        when(mockPayment.getExternalReference()).thenReturn("appointment|appt-1");
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (client, ctx) -> when(client.get(anyLong())).thenReturn(mockPayment))) {

            service.execute(Map.of("type", "payment", "data", Map.of("id", "99999")));

            assertThat(appointment.getPaymentStatus()).isEqualTo(AppointmentPaymentStatus.PAID);
            verify(businessMetrics).recordPaymentSucceeded();
            verify(eventPublisher).publishPaymentSucceeded(any());
        }
    }

    @Test
    void execute_nonApprovedPayment_publishesFailed() throws Exception {
        Payment mockPayment = mock(Payment.class);
        when(mockPayment.getStatus()).thenReturn("rejected");
        when(mockPayment.getExternalReference()).thenReturn("appointment|appt-1");
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (client, ctx) -> when(client.get(anyLong())).thenReturn(mockPayment))) {

            service.execute(Map.of("type", "payment", "data", Map.of("id", "99999")));

            verify(businessMetrics).recordPaymentFailed();
            verify(eventPublisher).publishPaymentFailed(any());
        }
    }

    @Test
    void execute_refNotStartingWithAppointment_doesNotProcess() throws Exception {
        Payment mockPayment = mock(Payment.class);
        when(mockPayment.getStatus()).thenReturn("approved");
        when(mockPayment.getExternalReference()).thenReturn("subscription|user-1");

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (client, ctx) -> when(client.get(anyLong())).thenReturn(mockPayment))) {

            service.execute(Map.of("type", "payment", "data", Map.of("id", "99999")));

            verifyNoInteractions(appointmentRepository);
        }
    }

    @Test
    void execute_nullExternalRef_doesNotProcess() throws Exception {
        Payment mockPayment = mock(Payment.class);
        when(mockPayment.getStatus()).thenReturn("approved");
        when(mockPayment.getExternalReference()).thenReturn(null);

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (client, ctx) -> when(client.get(anyLong())).thenReturn(mockPayment))) {

            service.execute(Map.of("type", "payment", "data", Map.of("id", "99999")));

            verifyNoInteractions(appointmentRepository);
        }
    }
}
