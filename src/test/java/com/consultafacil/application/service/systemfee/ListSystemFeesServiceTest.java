package com.consultafacil.application.service.systemfee;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.port.out.systemfee.SystemFeeRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSystemFeesServiceTest {

    @Mock SystemFeeRepositoryPort systemFeeRepository;

    @Test
    void execute_mapsEveryFeeIncludingInactiveOnes() {
        when(systemFeeRepository.findAll()).thenReturn(List.of(
                SystemFee.builder().id("a").paymentType(PaymentType.CONSULTATION).active(true).build(),
                SystemFee.builder().id("b").paymentType(PaymentType.EXAM).active(false).build()));

        List<SystemFeeResponseDTO> result = new ListSystemFeesService(systemFeeRepository, new SystemFeeMapper()).execute();

        assertThat(result).extracting(SystemFeeResponseDTO::getPaymentType)
                .containsExactly(PaymentType.CONSULTATION, PaymentType.EXAM);
        assertThat(result).extracting(SystemFeeResponseDTO::isActive).containsExactly(true, false);
    }

    @Test
    void execute_returnsEmptyListWhenNoFeesConfigured() {
        when(systemFeeRepository.findAll()).thenReturn(List.of());

        assertThat(new ListSystemFeesService(systemFeeRepository, new SystemFeeMapper()).execute()).isEmpty();
    }
}
