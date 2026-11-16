package com.consultafacil.application.service.systemfee;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.api.dto.billing.systemfee.UpdateSystemFeeDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.port.out.systemfee.SystemFeeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSystemFeeServiceTest {

    @Mock SystemFeeRepositoryPort systemFeeRepository;

    UpdateSystemFeeService service;

    @BeforeEach
    void setUp() {
        service = new UpdateSystemFeeService(systemFeeRepository, new SystemFeeMapper());
    }

    private SystemFee existing() {
        return SystemFee.builder()
                .id("fee-1")
                .paymentType(PaymentType.CONSULTATION)
                .fixedFee(new BigDecimal("1.50"))
                .percentageFee(new BigDecimal("3.00"))
                .active(true)
                .build();
    }

    @Test
    void execute_updatesOnlyProvidedFields() {
        when(systemFeeRepository.findById("fee-1")).thenReturn(Optional.of(existing()));
        when(systemFeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UpdateSystemFeeDTO dto = new UpdateSystemFeeDTO();
        dto.setPercentageFee(new BigDecimal("4.25"));

        SystemFeeResponseDTO result = service.execute("fee-1", dto);

        assertThat(result.getPercentageFee()).isEqualByComparingTo("4.25");
        assertThat(result.getFixedFee()).isEqualByComparingTo("1.50");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void execute_canDeactivateAndZeroTheFixedFee() {
        when(systemFeeRepository.findById("fee-1")).thenReturn(Optional.of(existing()));
        when(systemFeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        UpdateSystemFeeDTO dto = new UpdateSystemFeeDTO();
        dto.setFixedFee(BigDecimal.ZERO);
        dto.setActive(false);

        SystemFeeResponseDTO result = service.execute("fee-1", dto);

        assertThat(result.getFixedFee()).isEqualByComparingTo("0");
        assertThat(result.isActive()).isFalse();
        assertThat(result.getPercentageFee()).isEqualByComparingTo("3.00");
    }

    @Test
    void execute_throwsAndDoesNotSaveWhenMissing() {
        when(systemFeeRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("x", new UpdateSystemFeeDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(systemFeeRepository, never()).save(any());
    }
}
