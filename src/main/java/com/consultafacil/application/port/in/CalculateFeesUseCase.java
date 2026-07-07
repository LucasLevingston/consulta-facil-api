package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.fees.FeeCalculationResponseDTO;
import com.consultafacil.api.dto.fees.FeeConfigDTO;
import com.consultafacil.domain.enums.PaymentMethod;

import java.math.BigDecimal;

public interface CalculateFeesUseCase {

    FeeConfigDTO getConfig(String userId);

    FeeCalculationResponseDTO calculate(BigDecimal amount, PaymentMethod paymentMethod,
                                        boolean professionalAbsorbsFees, String userId);
}
