package com.consultafacil.adapter.out.email;

import com.consultafacil.domain.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpEmailAdapter implements EmailPort {

    @Override
    public void sendEmail(String to, String subject, String htmlBody, String textBody) {
        log.warn("[Email] AWS desabilitado — e-mail não enviado: '{}' para {}", subject, to);
    }

    @Override
    public void sendPasswordReset(String to, String name, String resetUrl) {
        log.warn("[Email] AWS desabilitado — reset de senha não enviado para {}", to);
    }

    @Override
    public void sendAppointmentConfirmation(String to, String patientName, String professionalName,
                                            String dateStr, String modality) {
        log.warn("[Email] AWS desabilitado — confirmação de consulta não enviada para {}", to);
    }

    @Override
    public void sendAppointmentCancellation(String to, String recipientName, String professionalName,
                                            String dateStr) {
        log.warn("[Email] AWS desabilitado — cancelamento de consulta não enviado para {}", to);
    }

    @Override
    public void sendAppointmentConfirmedByProfessional(String to, String patientName, String professionalName,
                                                        String dateStr) {
        log.warn("[Email] AWS desabilitado — confirmação pelo profissional não enviada para {}", to);
    }

    @Override
    public void sendAppointmentReminder(String to, String patientName, String professionalName,
                                        String dateStr, String modality) {
        log.warn("[Email] AWS desabilitado — lembrete de consulta não enviado para {}", to);
    }

    @Override
    public void sendPaymentReceipt(String to, String patientName, String appointmentId,
                                   String amount, String paymentMethod) {
        log.warn("[Email] AWS desabilitado — recibo de pagamento não enviado para {}", to);
    }

    @Override
    public void sendPaymentFailure(String to, String patientName, String appointmentId) {
        log.warn("[Email] AWS desabilitado — falha de pagamento não enviada para {}", to);
    }

    @Override
    public void sendMagicLink(String to, String name, String magicUrl) {
        log.warn("[Email] AWS desabilitado — magic link não enviado para {}", to);
    }

    @Override
    public void sendSubscriptionExpired(String to, String name, String planLabel, String renewUrl) {
        log.warn("[Email] AWS desabilitado — aviso de assinatura expirada não enviado para {}", to);
    }

    @Override
    public void sendSubscriptionRenewalReminder(String to, String name, String planLabel, int daysLeft, String renewUrl) {
        log.warn("[Email] AWS desabilitado — lembrete de renovação não enviado para {}", to);
    }

    @Override
    public void sendSubscriptionRenewed(String to, String name, String planLabel, String amount, String nextBillingDate) {
        log.warn("[Email] AWS desabilitado — confirmação de renovação não enviada para {}", to);
    }

    @Override
    public void sendPrescriptionAvailable(String to, String patientName, String downloadUrl) {
        log.warn("[Email] AWS desabilitado — receita digital não enviada para {}", to);
    }
}
