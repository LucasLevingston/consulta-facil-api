package com.consultafacil.application.port.in.appointment;

import java.util.Map;

public interface HandlePaymentWebhookUseCase {

    void execute(Map<String, Object> body);
}
