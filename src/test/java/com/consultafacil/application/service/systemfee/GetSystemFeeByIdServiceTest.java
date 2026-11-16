package com.consultafacil.application.service.systemfee;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.port.out.systemfee.SystemFeeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSystemFeeByIdServiceTest {

    @Mock SystemFeeRepositoryPort systemFeeRepository;

    GetSystemFeeByIdService service;

    @BeforeEach
    void setUp() {
        service = new GetSystemFeeByIdService(systemFeeRepository, new SystemFeeMapper());
    }

    @Test
    void execute_returnsMappedFee() {
        when(systemFeeRepository.findById("fee-1"))
                .thenReturn(Optional.of(SystemFee.builder().id("fee-1").paymentType(PaymentType.EXAM).build()));

        var result = service.execute("fee-1");

        assertThat(result.getId()).isEqualTo("fee-1");
        assertThat(result.getPaymentType()).isEqualTo(PaymentType.EXAM);
    }

    @Test
    void execute_throwsWhenMissing() {
        when(systemFeeRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SystemFee");
    }
}
