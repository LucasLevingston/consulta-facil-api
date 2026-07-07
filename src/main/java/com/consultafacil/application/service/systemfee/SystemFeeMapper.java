package com.consultafacil.application.service.systemfee;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.domain.entity.SystemFee;
import org.springframework.stereotype.Component;

@Component
public class SystemFeeMapper {

    public SystemFeeResponseDTO toDTO(SystemFee f) {
        return SystemFeeResponseDTO.builder()
                .id(f.getId())
                .paymentType(f.getPaymentType())
                .fixedFee(f.getFixedFee())
                .percentageFee(f.getPercentageFee())
                .active(f.isActive())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
