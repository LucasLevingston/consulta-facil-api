package com.consultafacil.api.dto.billing.payment;

import com.consultafacil.domain.enums.OwnerType;
import com.consultafacil.domain.enums.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBillingPaymentDTO {

    @NotNull(message = "paymentType é obrigatório")
    private PaymentType paymentType;

    private String referenceId;

    private OwnerType ownerType;
    private String ownerId;

    @NotNull(message = "amount é obrigatório")
    @DecimalMin(value = "0.01", message = "amount deve ser maior que zero")
    private BigDecimal amount;

    private String paymentMethod;
    private String gateway;

    @NotNull(message = "payerId é obrigatório")
    private String payerId;

    private String payerName;
    private String payerEmail;
    private String description;
}
