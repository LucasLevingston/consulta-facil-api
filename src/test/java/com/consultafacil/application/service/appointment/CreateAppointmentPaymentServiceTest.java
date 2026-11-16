package com.consultafacil.application.service.appointment;

import com.consultafacil.core.config.MercadoPagoConfig;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAppointmentPaymentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock MercadoPagoConfig mpConfig;

    @InjectMocks CreateAppointmentPaymentService service;

    PatientProfile patient;
    ProfessionalProfile professional;
    Appointment appointment;

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@email.com").name("João").password("x").role(UserRole.PATIENT).build();
        patient = new PatientProfile();
        patient.setId("pp-1");
        patient.setUser(pUser);

        User dUser = User.builder().id("u-2").email("dr@email.com").name("Dra. Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("prof-1");
        professional.setUser(dUser);
        professional.setSpecialty(Specialty.CARDIOLOGIA);
        professional.setConsultationPrice(new BigDecimal("250.00"));
        professional.setAcceptedPaymentMethods(new HashSet<>());

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setPaymentStatus(AppointmentPaymentStatus.UNPAID);
    }

    @Test
    void execute_patientNotFound_throwsNotFound() {
        when(patientProfileRepository.findByUserId("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("appt-1", "bad", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_appointmentNotFound_throwsNotFound() {
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("bad", "u-1", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_wrongPatient_throwsBadRequest() {
        PatientProfile other = new PatientProfile();
        other.setId("pp-other");
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(other));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> service.execute("appt-1", "u-1", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("belong to this patient");
    }

    @Test
    void execute_canceledAppointment_throwsBadRequest() {
        appointment.setStatus(AppointmentStatus.CANCELED);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> service.execute("appt-1", "u-1", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("canceled or completed");
    }

    @Test
    void execute_completedAppointment_throwsBadRequest() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> service.execute("appt-1", "u-1", null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void execute_alreadyPaid_throwsBadRequest() {
        appointment.setPaymentStatus(AppointmentPaymentStatus.PAID);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> service.execute("appt-1", "u-1", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already paid");
    }

    private void ownAppointment() {
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
    }

    /** Runs the service with MercadoPago's PreferenceClient stubbed and returns the request it received. */
    private PreferenceRequest payAndCapturePreference(BigDecimal callerAmount) throws Exception {
        Preference preference = mock(Preference.class);
        when(preference.getId()).thenReturn("pref-1");
        when(preference.getInitPoint()).thenReturn("https://mp/checkout/pref-1");
        ArgumentCaptor<PreferenceRequest> request = ArgumentCaptor.forClass(PreferenceRequest.class);
        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (client, ctx) -> when(client.create(any(PreferenceRequest.class))).thenReturn(preference))) {
            var response = service.execute("appt-1", "u-1", callerAmount);
            assertThat(response.getCheckoutUrl()).isEqualTo("https://mp/checkout/pref-1");
            verify(mocked.constructed().get(0)).create(request.capture());
        }
        return request.getValue();
    }

    @Test
    void execute_rejectsCallerAmountThatDiffersFromThePrice() {
        ownAppointment();

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class)) {
            assertThatThrownBy(() -> service.execute("appt-1", "u-1", new BigDecimal("0.01")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not match");
            assertThat(mocked.constructed()).isEmpty();
        }
        assertThat(appointment.getPaymentAmount()).isNull();
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void execute_chargesTheAppointmentPriceSetAtScheduling() throws Exception {
        appointment.setPaymentAmount(new BigDecimal("300.00"));
        ownAppointment();

        PreferenceRequest request = payAndCapturePreference(null);

        assertThat(request.getItems().get(0).getUnitPrice()).isEqualByComparingTo("300.00");
        assertThat(request.getExternalReference()).isEqualTo("appointment|appt-1");
        assertThat(appointment.getPaymentAmount()).isEqualByComparingTo("300.00");
        assertThat(appointment.getPaymentStatus()).isEqualTo(AppointmentPaymentStatus.PENDING_PAYMENT);
    }

    @Test
    void execute_fallsBackToTheConsultationPriceAndAcceptsAMatchingCallerAmount() throws Exception {
        ownAppointment();

        PreferenceRequest request = payAndCapturePreference(new BigDecimal("250"));

        assertThat(request.getItems().get(0).getUnitPrice()).isEqualByComparingTo("250.00");
        assertThat(appointment.getPaymentAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void execute_withoutAnyPrice_throwsBadRequest() {
        professional.setConsultationPrice(null);
        ownAppointment();

        assertThatThrownBy(() -> service.execute("appt-1", "u-1", new BigDecimal("100.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid payment amount");
    }
}
