package com.example.consulta.domain.port.out;

public interface EmailPort {

    void sendEmail(String to, String subject, String htmlBody, String textBody);

    void sendPasswordReset(String to, String name, String resetUrl);

    void sendAppointmentConfirmation(String to, String patientName, String professionalName,
                                     String dateStr, String modality);

    void sendAppointmentCancellation(String to, String recipientName, String professionalName,
                                     String dateStr);

    void sendAppointmentConfirmedByProfessional(String to, String patientName, String professionalName,
                                                String dateStr);

    void sendAppointmentReminder(String to, String patientName, String professionalName,
                                 String dateStr, String modality);

    void sendPaymentReceipt(String to, String patientName, String appointmentId,
                            String amount, String paymentMethod);

    void sendPaymentFailure(String to, String patientName, String appointmentId);
}
