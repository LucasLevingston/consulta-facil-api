package com.consultafacil.application.port.in.referral;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;

public interface GetOrCreateReferralCodeUseCase {

    ReferralCodeDTO execute(String userId);
}
