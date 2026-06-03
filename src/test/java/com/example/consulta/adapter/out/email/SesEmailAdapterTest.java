package com.example.consulta.adapter.out.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesEmailAdapterTest {

    @Mock
    SesV2Client sesClient;

    SesEmailAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SesEmailAdapter(sesClient, "noreply@consulta-facil.com");
    }

    @Test
    void sendEmail_shouldCallSesClientWithCorrectRecipient() {
        adapter.sendEmail("to@email.com", "Subject", "<p>html</p>", "text");

        var captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail((SendEmailRequest) captor.capture());
        assertThat(captor.getValue().destination().toAddresses()).containsExactly("to@email.com");
    }

    @Test
    void sendEmail_shouldUseConfiguredFromAddress() {
        adapter.sendEmail("to@email.com", "Subject", "<p>html</p>", "text");

        var captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail((SendEmailRequest) captor.capture());
        assertThat(captor.getValue().fromEmailAddress()).isEqualTo("noreply@consulta-facil.com");
    }

    @Test
    void sendEmail_sesThrows_shouldNotPropagateException() {
        doThrow(SesV2Exception.builder().message("SES error").build())
                .when(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());

        adapter.sendEmail("to@email.com", "Subject", "<p>html</p>", "text");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendPasswordReset_shouldDelegateToSendEmail() {
        adapter.sendPasswordReset("user@email.com", "Lucas", "https://app/reset?token=abc");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendAppointmentConfirmation_shouldDelegateToSendEmail() {
        adapter.sendAppointmentConfirmation("p@email.com", "Lucas", "Dra. Ana", "10/06 14:00", "IN_PERSON");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendAppointmentCancellation_shouldDelegateToSendEmail() {
        adapter.sendAppointmentCancellation("p@email.com", "Lucas", "Dra. Ana", "10/06 14:00");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendAppointmentConfirmedByProfessional_shouldDelegateToSendEmail() {
        adapter.sendAppointmentConfirmedByProfessional("p@email.com", "Lucas", "Dra. Ana", "10/06 14:00");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendPaymentReceipt_shouldDelegateToSendEmail() {
        adapter.sendPaymentReceipt("p@email.com", "Lucas", "appt-1", "250.00", "credit_card");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendPaymentFailure_shouldDelegateToSendEmail() {
        adapter.sendPaymentFailure("p@email.com", "Lucas", "appt-1");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendMagicLink_shouldDelegateToSendEmail() {
        adapter.sendMagicLink("p@email.com", "Lucas", "https://app/magic?token=xyz");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendSubscriptionExpired_shouldDelegateToSendEmail() {
        adapter.sendSubscriptionExpired("p@email.com", "Lucas", "Pro", "https://app/billing");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendSubscriptionRenewalReminder_singularDay_shouldDelegateToSendEmail() {
        adapter.sendSubscriptionRenewalReminder("p@email.com", "Lucas", "Pro", 1, "https://app/billing");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendSubscriptionRenewalReminder_pluralDays_shouldDelegateToSendEmail() {
        adapter.sendSubscriptionRenewalReminder("p@email.com", "Lucas", "Pro", 7, "https://app/billing");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendSubscriptionRenewed_shouldDelegateToSendEmail() {
        adapter.sendSubscriptionRenewed("p@email.com", "Lucas", "Pro", "99.90", "2026-07-01");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }

    @Test
    void sendAppointmentReminder_shouldDelegateToSendEmail() {
        adapter.sendAppointmentReminder("p@email.com", "Lucas", "Dra. Ana", "10/06 14:00", "IN_PERSON");

        verify(sesClient).sendEmail(ArgumentMatchers.<SendEmailRequest>any());
    }
}
