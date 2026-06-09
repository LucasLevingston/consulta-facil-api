package com.consultafacil.api.dto.seller;

import com.consultafacil.domain.enums.SellerStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SellerResponseDTO {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String slug;
    private BigDecimal commissionRate;
    private SellerStatus status;
    private String pixKey;
    private String notes;
    private LocalDateTime createdAt;

    // Aggregated metrics (populated in admin list view)
    private Long totalSales;
    private BigDecimal totalCommission;
    private BigDecimal pendingCommission;

    // Referral link (populated when serving the seller)
    private String referralLink;
}
