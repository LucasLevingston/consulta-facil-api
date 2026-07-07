package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.application.port.in.ListMyBillingPaymentsUseCase;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListMyBillingPaymentsService implements ListMyBillingPaymentsUseCase {

    private final BillingPaymentRepositoryPort paymentRepository;
    private final BillingPaymentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<BillingPaymentResponseDTO> execute(String payerId) {
        return paymentRepository.findByPayerId(payerId).stream().map(mapper::toDTO).collect(Collectors.toList());
    }
}
