package com.consultafacil.api.dto.billing.systemfee;

import com.consultafacil.domain.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SystemFeeResponseDTO {
    private String id;
    private PaymentType paymentType;
    private BigDecimal fixedFee;
    private BigDecimal percentageFee;
    private boolean active;
    private LocalDateTime updatedAt;
}
