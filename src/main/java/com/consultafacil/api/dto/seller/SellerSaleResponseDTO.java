package com.consultafacil.api.dto.seller;

import com.consultafacil.domain.enums.SellerSaleStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SellerSaleResponseDTO {
    private String id;
    private String sellerId;
    private String subscriptionId;
    private BigDecimal grossAmount;
    private BigDecimal commissionAmount;
    private LocalDate monthReference;
    private SellerSaleStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
