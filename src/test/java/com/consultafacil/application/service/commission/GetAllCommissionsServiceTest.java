package com.consultafacil.application.service.commission;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;
import com.consultafacil.application.service.referral.ReferralCommissionMapper;
import com.consultafacil.domain.entity.ReferralCommission;
import com.consultafacil.domain.enums.CommissionStatus;
import com.consultafacil.domain.port.out.referral.ReferralCommissionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllCommissionsServiceTest {

    @Mock ReferralCommissionRepositoryPort commissionRepository;

    @Test
    void execute_mapsAllCommissions() {
        when(commissionRepository.findAll()).thenReturn(List.of(
                ReferralCommission.builder().id("a").amount(BigDecimal.ONE).status(CommissionStatus.PENDING).build(),
                ReferralCommission.builder().id("b").amount(BigDecimal.TEN).status(CommissionStatus.AVAILABLE).build()));

        List<ReferralCommissionDTO> result =
                new GetAllCommissionsService(commissionRepository, new ReferralCommissionMapper()).execute();

        assertThat(result).extracting(ReferralCommissionDTO::getId).containsExactly("a", "b");
        assertThat(result).extracting(ReferralCommissionDTO::getStatus)
                .containsExactly(CommissionStatus.PENDING, CommissionStatus.AVAILABLE);
        assertThat(result.get(0).getPercentage()).isEqualByComparingTo("10");
    }

    @Test
    void execute_returnsEmptyListWhenNoCommissions() {
        when(commissionRepository.findAll()).thenReturn(List.of());

        assertThat(new GetAllCommissionsService(commissionRepository, new ReferralCommissionMapper()).execute()).isEmpty();
    }
}
