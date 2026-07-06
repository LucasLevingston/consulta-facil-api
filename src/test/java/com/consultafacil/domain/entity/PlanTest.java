package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PlanTest {

    @Test
    void durationDays_monthly_returns30() {
        Plan monthly = Plan.builder().id("plan-pro-m").slug("monthly").name("Pro Mensal")
                .tier("PRO").billingPeriod(BillingPeriod.MONTHLY).price(new BigDecimal("149.90"))
                .frequency(1).frequencyType("months").status(PlanStatus.ACTIVE).displayOrder(4).build();
        assertThat(monthly.durationDays()).isEqualTo(30);
    }

    @Test
    void durationDays_annual_returns365() {
        Plan annual = Plan.builder().id("a").slug("yearly").name("Anual").tier("PRO")
                .billingPeriod(BillingPeriod.ANNUAL).price(BigDecimal.TEN)
                .frequency(12).frequencyType("months").status(PlanStatus.ACTIVE).displayOrder(1).build();
        assertThat(annual.durationDays()).isEqualTo(365);
    }

    @Test
    void durationDays_semiannual_returns180() {
        Plan semi = Plan.builder().id("s").slug("semi").name("Semi").tier("PRO")
                .billingPeriod(BillingPeriod.SEMIANNUAL).price(BigDecimal.TEN)
                .frequency(6).frequencyType("months").status(PlanStatus.ACTIVE).displayOrder(2).build();
        assertThat(semi.durationDays()).isEqualTo(180);
    }
}
