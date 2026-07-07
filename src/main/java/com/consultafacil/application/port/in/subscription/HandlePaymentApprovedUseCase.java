package com.consultafacil.application.port.in.subscription;

public interface HandlePaymentApprovedUseCase {
    void execute(String paymentId, String externalReference);
}
