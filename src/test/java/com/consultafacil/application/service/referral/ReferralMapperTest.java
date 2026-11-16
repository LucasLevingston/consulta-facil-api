package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.referral.ReferralDTO;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.enums.ReferralStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReferralMapperTest {

    @Test
    void toDTO_copiesEveryField() {
        LocalDateTime created = LocalDateTime.of(2026, 3, 4, 5, 6);
        Referral referral = Referral.builder()
                .id("r1").referrerId("a").referredId("b").referralCodeId("rc").firstPaymentId("p1")
                .status(ReferralStatus.APPROVED).createdAt(created).build();

        ReferralDTO dto = new ReferralMapper().toDTO(referral);

        assertThat(dto.getId()).isEqualTo("r1");
        assertThat(dto.getReferrerId()).isEqualTo("a");
        assertThat(dto.getReferredId()).isEqualTo("b");
        assertThat(dto.getReferralCodeId()).isEqualTo("rc");
        assertThat(dto.getFirstPaymentId()).isEqualTo("p1");
        assertThat(dto.getStatus()).isEqualTo(ReferralStatus.APPROVED);
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }
}
