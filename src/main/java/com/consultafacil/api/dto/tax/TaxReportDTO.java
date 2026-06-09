package com.consultafacil.api.dto.tax;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class TaxReportDTO {
    private String month;
    private BigDecimal totalGross;
    private BigDecimal totalProcessingFees;
    private BigDecimal totalTax;
    private BigDecimal totalIss;
    private BigDecimal totalNet;
    private long transactionCount;
    private String taxRegime;
    private BigDecimal taxRate;
    private Map<String, BigDecimal> byPaymentMethod;
}
