package com.consultafacil.core.messaging;

public final class RabbitMQConstants {

    private RabbitMQConstants() {
    }

    public static final String EXCHANGE = "consulta-facil";
    public static final String DLX = "consulta-facil.dlx";
    public static final String DLQ = "consulta-facil.dead-letter";

    public static final String Q_APPOINTMENTS_CREATED_EMAIL = "appointments.created.email";
    public static final String Q_APPOINTMENTS_CREATED_WHATSAPP = "appointments.created.whatsapp";
    public static final String Q_APPOINTMENTS_CANCELED_EMAIL = "appointments.canceled.email";
    public static final String Q_APPOINTMENTS_CANCELED_WHATSAPP = "appointments.canceled.whatsapp";
    public static final String Q_APPOINTMENTS_CONFIRMED_EMAIL = "appointments.confirmed.email";
    public static final String Q_APPOINTMENTS_CONFIRMED_WHATSAPP = "appointments.confirmed.whatsapp";
    public static final String Q_PAYMENTS_SUCCEEDED_EMAIL = "payments.succeeded.email";
    public static final String Q_PAYMENTS_FAILED_EMAIL = "payments.failed.email";

    public static final String RK_APPOINTMENTS_CREATED = "appointments.created";
    public static final String RK_APPOINTMENTS_CANCELED = "appointments.canceled";
    public static final String RK_APPOINTMENTS_CONFIRMED = "appointments.confirmed";
    public static final String RK_PAYMENTS_SUCCEEDED = "payments.succeeded";
    public static final String RK_PAYMENTS_FAILED = "payments.failed";
}
