package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.CommissionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReferralCommissionMapperTest {

    @Test
    void toDTO_copiesEveryField() {
        LocalDateTime created = LocalDateTime.of(2026, 3, 4, 5, 6);
        ReferralCommission commission = ReferralCommission.builder()
                .id("c").referralId("r1").paymentId("p1").amount(new BigDecimal("9.99"))
                .percentage(new BigDecimal("10")).availableAt(created.plusDays(30))
                .status(CommissionStatus.PAID).createdAt(created).build();

        ReferralCommissionDTO dto = new ReferralCommissionMapper().toDTO(commission);

        assertThat(dto.getId()).isEqualTo("c");
        assertThat(dto.getReferralId()).isEqualTo("r1");
        assertThat(dto.getPaymentId()).isEqualTo("p1");
        assertThat(dto.getAmount()).isEqualByComparingTo("9.99");
        assertThat(dto.getPercentage()).isEqualByComparingTo("10");
        assertThat(dto.getAvailableAt()).isEqualTo(created.plusDays(30));
        assertThat(dto.getStatus()).isEqualTo(CommissionStatus.PAID);
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }
}
