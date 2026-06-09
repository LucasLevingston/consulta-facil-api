package com.consultafacil.api.dto.seller;

import com.consultafacil.domain.enums.SellerStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SellerDashboardDTO {
    private String sellerId;
    private String slug;
    private String referralLink;
    private BigDecimal commissionRate;
    private SellerStatus status;
    private String pixKey;
    private LocalDateTime memberSince;

    private Long totalSales;
    private BigDecimal totalCommission;
    private BigDecimal pendingCommission;
    private BigDecimal paidCommission;

    private List<SellerSaleResponseDTO> recentSales;
}
