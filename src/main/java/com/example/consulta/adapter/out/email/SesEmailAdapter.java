package com.example.consulta.adapter.out.email;

import com.example.consulta.domain.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Slf4j
@Component
public class SesEmailAdapter implements EmailPort {

    private final SesV2Client sesClient;
    private final String fromEmail;

    public SesEmailAdapter(
            SesV2Client sesClient,
            @Value("${aws.ses.from-email:noreply@consulta-facil.com}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    @Override
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

    @Override
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

    @Override
    public void sendAppointmentConfirmation(String to, String patientName, String professionalName,
                                            String dateStr, String modality) {
        String html = """
                <h2>Consulta agendada</h2>
                <p>Olá, %s!</p>
                <p>Sua consulta com <strong>%s</strong> foi agendada para <strong>%s</strong> (%s).</p>
                <p>Acesse o app para mais detalhes.</p>
                """.formatted(patientName, professionalName, dateStr, modality);
        String text = "Olá, %s! Consulta com %s agendada para %s (%s).".formatted(patientName, professionalName, dateStr, modality);
        sendEmail(to, "Consulta agendada — Consulta Fácil", html, text);
    }

    @Override
    public void sendAppointmentCancellation(String to, String recipientName, String professionalName,
                                            String dateStr) {
        String html = """
                <h2>Consulta cancelada</h2>
                <p>Olá, %s!</p>
                <p>A consulta com <strong>%s</strong> em <strong>%s</strong> foi cancelada.</p>
                <p>Entre em contato para reagendar.</p>
                """.formatted(recipientName, professionalName, dateStr);
        String text = "Olá, %s! A consulta com %s em %s foi cancelada.".formatted(recipientName, professionalName, dateStr);
        sendEmail(to, "Consulta cancelada — Consulta Fácil", html, text);
    }

    @Override
    public void sendAppointmentConfirmedByProfessional(String to, String patientName, String professionalName,
                                                        String dateStr) {
        String html = """
                <h2>Consulta confirmada</h2>
                <p>Olá, %s!</p>
                <p>Sua consulta com <strong>%s</strong> em <strong>%s</strong> foi confirmada pelo profissional.</p>
                """.formatted(patientName, professionalName, dateStr);
        String text = "Olá, %s! Consulta com %s em %s confirmada.".formatted(patientName, professionalName, dateStr);
        sendEmail(to, "Consulta confirmada — Consulta Fácil", html, text);
    }

    @Override
    public void sendAppointmentReminder(String to, String patientName, String professionalName,
                                        String dateStr, String modality) {
        String html = """
                <h2>Lembrete de consulta</h2>
                <p>Olá, %s!</p>
                <p>Você tem uma consulta amanhã com <strong>%s</strong> às <strong>%s</strong> (%s).</p>
                <p>Prepare-se com antecedência!</p>
                """.formatted(patientName, professionalName, dateStr, modality);
        String text = "Lembrete: consulta amanhã com %s às %s (%s).".formatted(professionalName, dateStr, modality);
        sendEmail(to, "Lembrete de consulta — Consulta Fácil", html, text);
    }

    @Override
    public void sendPaymentReceipt(String to, String patientName, String appointmentId,
                                   String amount, String paymentMethod) {
        String html = """
                <h2>Pagamento confirmado</h2>
                <p>Olá, %s!</p>
                <p>Recebemos seu pagamento de <strong>R$ %s</strong> via <strong>%s</strong>.</p>
                <p>ID da consulta: %s</p>
                """.formatted(patientName, amount, paymentMethod, appointmentId);
        String text = "Pagamento de R$ %s via %s confirmado. Consulta: %s.".formatted(amount, paymentMethod, appointmentId);
        sendEmail(to, "Pagamento confirmado — Consulta Fácil", html, text);
    }

    @Override
    public void sendPaymentFailure(String to, String patientName, String appointmentId) {
        String html = """
                <h2>Falha no pagamento</h2>
                <p>Olá, %s!</p>
                <p>Não conseguimos processar seu pagamento para a consulta <strong>%s</strong>.</p>
                <p>Tente novamente ou entre em contato.</p>
                """.formatted(patientName, appointmentId);
        String text = "Falha no pagamento da consulta %s. Tente novamente.".formatted(appointmentId);
        sendEmail(to, "Falha no pagamento — Consulta Fácil", html, text);
    }

    @Override
    public void sendMagicLink(String to, String name, String magicUrl) {
        String html = """
                <h2>Seu link de acesso</h2>
                <p>Olá, %s!</p>
                <p>Clique no botão abaixo para entrar na sua conta. O link expira em 15 minutos e só pode ser usado uma vez.</p>
                <a href="%s" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;display:inline-block;">
                  Entrar na conta
                </a>
                <p>Se você não solicitou este link, ignore este e-mail.</p>
                """.formatted(name, magicUrl);
        String text = "Olá, %s! Acesse sua conta pelo link (válido por 15 min): %s".formatted(name, magicUrl);
        sendEmail(to, "Seu link de acesso — Consulta Fácil", html, text);
    }

    @Override
    public void sendSubscriptionExpired(String to, String name, String planLabel, String renewUrl) {
        String html = """
                <h2>Sua assinatura expirou</h2>
                <p>Olá, %s!</p>
                <p>Sua assinatura <strong>%s</strong> expirou. Renove agora para continuar usando a plataforma sem interrupções.</p>
                <a href="%s" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;display:inline-block;">
                  Renovar assinatura
                </a>
                """.formatted(name, planLabel, renewUrl);
        String text = "Olá, %s! Sua assinatura %s expirou. Renove em: %s".formatted(name, planLabel, renewUrl);
        sendEmail(to, "Assinatura expirada — Consulta Fácil", html, text);
    }

    @Override
    public void sendSubscriptionRenewalReminder(String to, String name, String planLabel, int daysLeft, String renewUrl) {
        String daysText = daysLeft == 1 ? "1 dia" : daysLeft + " dias";
        String html = """
                <h2>Sua assinatura expira em %s</h2>
                <p>Olá, %s!</p>
                <p>Seu plano <strong>%s</strong> expira em <strong>%s</strong>. A renovação será processada automaticamente — nenhuma ação necessária.</p>
                <p>Se desejar cancelar ou alterar seu plano, acesse as configurações antes da renovação.</p>
                <a href="%s" style="background:#2563eb;color:#fff;padding:12px 24px;border-radius:6px;text-decoration:none;display:inline-block;">
                  Gerenciar assinatura
                </a>
                """.formatted(daysText, name, planLabel, daysText, renewUrl);
        String text = "Olá, %s! Seu plano %s expira em %s. A renovação é automática. Gerencie em: %s"
                .formatted(name, planLabel, daysText, renewUrl);
        sendEmail(to, "Lembrete: assinatura renova em " + daysText + " — Consulta Fácil", html, text);
    }

    @Override
    public void sendSubscriptionRenewed(String to, String name, String planLabel, String amount, String nextBillingDate) {
        String html = """
                <h2>Assinatura renovada com sucesso</h2>
                <p>Olá, %s!</p>
                <p>Sua assinatura <strong>%s</strong> foi renovada automaticamente.</p>
                <table style="border-collapse:collapse;margin-top:16px;">
                  <tr><td style="padding:6px 16px 6px 0;color:#6b7280;">Valor cobrado</td><td style="padding:6px 0;font-weight:600;">R$ %s</td></tr>
                  <tr><td style="padding:6px 16px 6px 0;color:#6b7280;">Próxima renovação</td><td style="padding:6px 0;">%s</td></tr>
                </table>
                <p style="margin-top:16px;font-size:0.85em;color:#6b7280;">
                  Este e-mail serve como comprovante de pagamento (nota fiscal eletrônica será emitida separadamente).
                </p>
                """.formatted(name, planLabel, amount, nextBillingDate);
        String text = "Olá, %s! Assinatura %s renovada. Valor: R$ %s. Próxima renovação: %s."
                .formatted(name, planLabel, amount, nextBillingDate);
        sendEmail(to, "Assinatura renovada — Consulta Fácil", html, text);
    }
}
