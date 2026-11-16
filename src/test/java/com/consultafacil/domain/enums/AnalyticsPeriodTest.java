package com.consultafacil.domain.enums;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsPeriodTest {

    private static final LocalDateTime END = LocalDateTime.of(2026, 11, 16, 12, 0);

    @Test
    void fromCode_parsesKnownCodesCaseInsensitively() {
        assertThat(AnalyticsPeriod.fromCode("30d")).contains(AnalyticsPeriod.LAST_30_DAYS);
        assertThat(AnalyticsPeriod.fromCode("90D")).contains(AnalyticsPeriod.LAST_90_DAYS);
        assertThat(AnalyticsPeriod.fromCode("12m")).contains(AnalyticsPeriod.LAST_12_MONTHS);
    }

    @Test
    void fromCode_rejectsUnknownOrMissingCodes() {
        assertThat(AnalyticsPeriod.fromCode("1y")).isEmpty();
        assertThat(AnalyticsPeriod.fromCode("")).isEmpty();
        assertThat(AnalyticsPeriod.fromCode(null)).isEmpty();
    }

    @Test
    void startFrom_subtractsTheWindow() {
        assertThat(AnalyticsPeriod.LAST_30_DAYS.startFrom(END)).isEqualTo(LocalDateTime.of(2026, 10, 17, 12, 0));
        assertThat(AnalyticsPeriod.LAST_90_DAYS.startFrom(END)).isEqualTo(LocalDateTime.of(2026, 8, 18, 12, 0));
        assertThat(AnalyticsPeriod.LAST_12_MONTHS.startFrom(END)).isEqualTo(LocalDateTime.of(2025, 11, 16, 12, 0));
    }

    @Test
    void getCode_isTheValueAcceptedByTheApi() {
        assertThat(AnalyticsPeriod.LAST_12_MONTHS.getCode()).isEqualTo("12m");
    }
}
