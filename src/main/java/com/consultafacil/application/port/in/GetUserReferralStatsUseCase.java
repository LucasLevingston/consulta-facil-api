package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.referral.ReferralStatsDTO;

public interface GetUserReferralStatsUseCase {

    ReferralStatsDTO execute(String userId);
}
