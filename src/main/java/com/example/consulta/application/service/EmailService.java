package com.example.consulta.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Slf4j
@Service
public class EmailService {

    private final SesV2Client sesClient;
    private final String fromEmail;

    public EmailService(
            SesV2Client sesClient,
            @Value("${aws.ses.from-email:noreply@consulta-facil.com}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    public void sendEmail(String to, String subject, String htmlBody, String textBody) {
        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .fromEmailAddress(fromEmail)
                    .destination(Destination.builder().toAddresses(to).build())
                    .content(EmailContent.builder()
                            .simple(Message.builder()
                                    .subject(Content.builder().data(subject).charset("UTF-8").build())
                                    .body(Body.builder()
                                            .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                            .text(Content.builder().data(textBody).charset("UTF-8").build())
                                            .build())
                                    .build())
                            .build())
                    .build());
            log.info("[Email] Sent '{}' to {}", subject, to);
        } catch (Exception e) {
            log.error("[Email] Failed to send '{}' to {}: {}", subject, to, e.getMessage());
        }
    }

    public void sendPasswordReset(String to, String name, String resetUrl) {
        String html = """
                <h2>Redefinição de senha</h2>
                <p>Olá, %s!</p>
                <p>Clique no botão abaixo para redefinir sua senha. O link expira em 1 hora.</p>
                <a href="%s" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;display:inline-block;">
                  Redefinir senha
                </a>
                <p>Se você não solicitou a redefinição, ignore este e-mail.</p>
                """.formatted(name, resetUrl);
        String text = "Olá, %s! Acesse o link para redefinir sua senha (válido por 1 hora): %s".formatted(name, resetUrl);
        sendEmail(to, "Redefinição de senha — Consulta Fácil", html, text);
    }

    public void sendAppointmentConfirmation(String to, String patientName, String professionalName,
                                            String dateStr, String modality) {
        String html = """
                <h2>Consulta agendada ✅</h2>
                <p>Olá, %s!</p>
                <p>Sua consulta com <strong>%s</strong> foi agendada para <strong>%s</strong> (%s).</p>
                <p>Acesse o app para mais detalhes.</p>
                """.formatted(patientName, professionalName, dateStr, modality);
        String text = "Olá, %s! Consulta com %s agendada para %s (%s).".formatted(patientName, professionalName, dateStr, modality);
        sendEmail(to, "Consulta agendada — Consulta Fácil", html, text);
    }

    public void sendAppointmentCancellation(String to, String recipientName, String professionalName,
                                            String dateStr) {
        String html = """
                <h2>Consulta cancelada ❌</h2>
                <p>Olá, %s!</p>
                <p>A consulta com <strong>%s</strong> em <strong>%s</strong> foi cancelada.</p>
                <p>Entre em contato para reagendar.</p>
                """.formatted(recipientName, professionalName, dateStr);
        String text = "Olá, %s! A consulta com %s em %s foi cancelada.".formatted(recipientName, professionalName, dateStr);
        sendEmail(to, "Consulta cancelada — Consulta Fácil", html, text);
    }

    public void sendAppointmentConfirmedByProfessional(String to, String patientName, String professionalName,
                                                        String dateStr) {
        String html = """
                <h2>Consulta confirmada 🗓️</h2>
                <p>Olá, %s!</p>
                <p>Sua consulta com <strong>%s</strong> em <strong>%s</strong> foi confirmada pelo profissional.</p>
                """.formatted(patientName, professionalName, dateStr);
        String text = "Olá, %s! Consulta com %s em %s confirmada.".formatted(patientName, professionalName, dateStr);
        sendEmail(to, "Consulta confirmada — Consulta Fácil", html, text);
    }

    public void sendAppointmentReminder(String to, String patientName, String professionalName,
                                        String dateStr, String modality) {
        String html = """
                <h2>Lembrete de consulta ⏰</h2>
                <p>Olá, %s!</p>
                <p>Você tem uma consulta amanhã com <strong>%s</strong> às <strong>%s</strong> (%s).</p>
                <p>Prepare-se com antecedência!</p>
                """.formatted(patientName, professionalName, dateStr, modality);
        String text = "Lembrete: consulta amanhã com %s às %s (%s).".formatted(professionalName, dateStr, modality);
        sendEmail(to, "Lembrete de consulta — Consulta Fácil", html, text);
    }

    public void sendPaymentReceipt(String to, String patientName, String appointmentId,
                                   String amount, String paymentMethod) {
        String html = """
                <h2>Pagamento confirmado 💳</h2>
                <p>Olá, %s!</p>
                <p>Recebemos seu pagamento de <strong>R$ %s</strong> via <strong>%s</strong>.</p>
                <p>ID da consulta: %s</p>
                """.formatted(patientName, amount, paymentMethod, appointmentId);
        String text = "Pagamento de R$ %s via %s confirmado. Consulta: %s.".formatted(amount, paymentMethod, appointmentId);
        sendEmail(to, "Pagamento confirmado — Consulta Fácil", html, text);
    }

    public void sendPaymentFailure(String to, String patientName, String appointmentId) {
        String html = """
                <h2>Falha no pagamento ⚠️</h2>
                <p>Olá, %s!</p>
                <p>Não conseguimos processar seu pagamento para a consulta <strong>%s</strong>.</p>
                <p>Tente novamente ou entre em contato.</p>
                """.formatted(patientName, appointmentId);
        String text = "Falha no pagamento da consulta %s. Tente novamente.".formatted(appointmentId);
        sendEmail(to, "Falha no pagamento — Consulta Fácil", html, text);
    }
}
