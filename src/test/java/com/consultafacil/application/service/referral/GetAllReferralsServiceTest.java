package com.consultafacil.application.service.referral;

import com.consultafacil.api.dto.billing.referral.ReferralDTO;
import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.port.out.referral.ReferralRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllReferralsServiceTest {

    @Mock ReferralRepositoryPort referralRepository;

    @Test
    void execute_mapsEveryReferral() {
        when(referralRepository.findAll()).thenReturn(List.of(
                Referral.builder().id("r1").referrerId("a").referredId("b").build(),
                Referral.builder().id("r2").referrerId("a").referredId("c").build()));

        List<ReferralDTO> result = new GetAllReferralsService(referralRepository, new ReferralMapper()).execute();

        assertThat(result).extracting(ReferralDTO::getReferredId).containsExactly("b", "c");
    }

    @Test
    void execute_returnsEmptyListWhenThereAreNoReferrals() {
        when(referralRepository.findAll()).thenReturn(List.of());

        assertThat(new GetAllReferralsService(referralRepository, new ReferralMapper()).execute()).isEmpty();
    }
}
