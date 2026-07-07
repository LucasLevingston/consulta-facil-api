package com.consultafacil.application.port.in;

public interface HandlePaymentApprovedUseCase {
    void execute(String paymentId, String externalReference);
}
