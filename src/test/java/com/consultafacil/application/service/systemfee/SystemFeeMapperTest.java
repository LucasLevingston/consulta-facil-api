package com.consultafacil.application.service.systemfee;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.PaymentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SystemFeeMapperTest {

    @Test
    void toDTO_copiesEveryField() {
        SystemFee fee = SystemFee.builder()
                .id("fee-1")
                .paymentType(PaymentType.CONSULTATION)
                .fixedFee(new BigDecimal("1.50"))
                .percentageFee(new BigDecimal("3.00"))
                .active(false)
                .updatedAt(LocalDateTime.of(2026, 1, 2, 3, 4))
                .build();

        SystemFeeResponseDTO dto = new SystemFeeMapper().toDTO(fee);

        assertThat(dto.getId()).isEqualTo("fee-1");
        assertThat(dto.getPaymentType()).isEqualTo(PaymentType.CONSULTATION);
        assertThat(dto.getFixedFee()).isEqualByComparingTo("1.50");
        assertThat(dto.getPercentageFee()).isEqualByComparingTo("3.00");
        assertThat(dto.isActive()).isFalse();
        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4));
    }
}
