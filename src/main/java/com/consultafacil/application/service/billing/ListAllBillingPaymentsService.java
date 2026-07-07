package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.application.port.in.ListAllBillingPaymentsUseCase;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListAllBillingPaymentsService implements ListAllBillingPaymentsUseCase {

    private final BillingPaymentRepositoryPort paymentRepository;
    private final BillingPaymentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<BillingPaymentResponseDTO> execute() {
        return paymentRepository.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }
}
