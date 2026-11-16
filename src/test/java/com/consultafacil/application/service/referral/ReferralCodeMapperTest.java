package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.domain.entity.ReferralCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReferralCodeMapperTest {

    @Test
    void toDTO_copiesEveryField() {
        LocalDateTime created = LocalDateTime.of(2026, 3, 4, 5, 6);
        ReferralCode code = ReferralCode.builder()
                .id("rc").userId("u").code("XYZ789").active(false).createdAt(created).build();

        ReferralCodeDTO dto = new ReferralCodeMapper().toDTO(code);

        assertThat(dto.getId()).isEqualTo("rc");
        assertThat(dto.getUserId()).isEqualTo("u");
        assertThat(dto.getCode()).isEqualTo("XYZ789");
        assertThat(dto.isActive()).isFalse();
        assertThat(dto.getCreatedAt()).isEqualTo(created);
    }
}
