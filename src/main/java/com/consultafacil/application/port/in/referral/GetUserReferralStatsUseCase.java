package com.consultafacil.application.port.in.referral;

import com.consultafacil.api.dto.billing.referral.ReferralStatsDTO;

public interface GetUserReferralStatsUseCase {

    ReferralStatsDTO execute(String userId);
}
