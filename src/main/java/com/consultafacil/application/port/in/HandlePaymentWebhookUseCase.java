package com.consultafacil.application.port.in;

import java.util.Map;

public interface HandlePaymentWebhookUseCase {

    void execute(Map<String, Object> body);
}
