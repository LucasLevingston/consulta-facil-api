package com.consultafacil.api.dto.professional;

import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.enums.PaymentTiming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentSettingsDTO {

    @NotNull(message = "Momento do pagamento é obrigatório")
    private PaymentTiming paymentTiming;

    @NotEmpty(message = "Ao menos um método de pagamento deve ser selecionado")
    private Set<PaymentMethod> acceptedPaymentMethods;
}
