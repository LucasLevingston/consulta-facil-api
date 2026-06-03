package com.example.consulta.application.port.in;

import java.util.Map;

public interface HandlePaymentWebhookUseCase {

    void execute(Map<String, Object> body);
}
