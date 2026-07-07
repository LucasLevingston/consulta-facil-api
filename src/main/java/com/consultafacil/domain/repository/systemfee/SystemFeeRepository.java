package com.consultafacil.domain.repository.systemfee;

import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemFeeRepository extends JpaRepository<SystemFee, String> {
    Optional<SystemFee> findByPaymentType(PaymentType paymentType);
    List<SystemFee> findAllByActiveTrue();
}
