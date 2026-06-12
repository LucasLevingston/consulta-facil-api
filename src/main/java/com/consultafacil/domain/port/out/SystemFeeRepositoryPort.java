package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.PaymentType;

import java.util.List;
import java.util.Optional;

public interface SystemFeeRepositoryPort {
    SystemFee save(SystemFee systemFee);
    Optional<SystemFee> findById(String id);
    Optional<SystemFee> findByPaymentType(PaymentType paymentType);
    List<SystemFee> findAll();
    List<SystemFee> findAllActive();
}
