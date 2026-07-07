package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.application.port.in.GetBillingPaymentByIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBillingPaymentByIdService implements GetBillingPaymentByIdUseCase {

    private final BillingPaymentFinder finder;
    private final BillingPaymentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public BillingPaymentResponseDTO execute(String id) {
        return mapper.toDTO(finder.findOrThrow(id));
    }
}
