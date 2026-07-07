package com.consultafacil.adapter.out.persistence.systemfee;

import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.port.out.systemfee.SystemFeeRepositoryPort;
import com.consultafacil.domain.repository.systemfee.SystemFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SystemFeeRepositoryAdapter implements SystemFeeRepositoryPort {

    private final SystemFeeRepository systemFeeRepository;

    @Override
    public SystemFee save(SystemFee systemFee) { return systemFeeRepository.save(systemFee); }

    @Override
    public Optional<SystemFee> findById(String id) { return systemFeeRepository.findById(id); }

    @Override
    public Optional<SystemFee> findByPaymentType(PaymentType paymentType) {
        return systemFeeRepository.findByPaymentType(paymentType);
    }

    @Override
    public List<SystemFee> findAll() { return systemFeeRepository.findAll(); }

    @Override
    public List<SystemFee> findAllActive() { return systemFeeRepository.findAllByActiveTrue(); }
}
