package com.consultafacil.application.port.in.subscription;

public interface HandlePreapprovalWebhookUseCase {
    void execute(String preapprovalId);
}
