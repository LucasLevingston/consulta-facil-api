package com.consultafacil.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "billing.tax")
@Data
public class TaxConfig {

    private String regime = "SIMPLES_NACIONAL";

    private Simples simples = new Simples();
    private Iss iss = new Iss();
    private ProcessingFee processingFee = new ProcessingFee();

    @Data
    public static class Simples {
        private BigDecimal rate = new BigDecimal("6.0");
    }

    @Data
    public static class Iss {
        private BigDecimal rate = new BigDecimal("2.9");
    }

    @Data
    public static class ProcessingFee {
        private BigDecimal creditCard = new BigDecimal("4.98");
        private BigDecimal pix = new BigDecimal("0.99");
        private BigDecimal debit = new BigDecimal("1.99");
    }
}
