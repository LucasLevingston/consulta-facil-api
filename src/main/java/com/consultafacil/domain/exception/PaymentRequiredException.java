package com.consultafacil.domain.exception;

public class PaymentRequiredException extends DomainException {

    private final String requiredPlan;

    public PaymentRequiredException(String requiredPlan) {
        super("PAYMENT_REQUIRED: Plano " + requiredPlan + " necessário para esta funcionalidade");
        this.requiredPlan = requiredPlan;
    }

    public String getRequiredPlan() {
        return requiredPlan;
    }
}
