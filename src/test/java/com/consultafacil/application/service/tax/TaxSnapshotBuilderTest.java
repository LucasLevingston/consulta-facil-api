package com.consultafacil.application.service.tax;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.core.config.TaxConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxSnapshotBuilderTest {

    @InjectMocks TaxSnapshotBuilder service;

    TaxConfig config;

    @BeforeEach
    void setUp() {
        config = new TaxConfig();
        // defaults: SIMPLES_NACIONAL 6%, ISS 2.9%, CC 4.98%, PIX 0.99%, debit 1.99%
        org.springframework.test.util.ReflectionTestUtils.setField(service, "taxConfig", config);
    }

    @Test
    void buildSnapshot_containsRegimeAndMethod() {
        TaxBreakdown b = new TaxBreakdown(
                new BigDecimal("149.90"), new BigDecimal("1.48"), new BigDecimal("8.99"),
                BigDecimal.ZERO, new BigDecimal("139.43"), new BigDecimal("6.0"),
                "SIMPLES_NACIONAL", "PIX");

        String snapshot = service.buildSnapshot(b);

        assertThat(snapshot).contains("SIMPLES_NACIONAL");
        assertThat(snapshot).contains("PIX");
    }
}
