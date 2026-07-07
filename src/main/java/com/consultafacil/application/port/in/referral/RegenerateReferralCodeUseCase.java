package com.consultafacil.application.port.in.referral;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;

public interface RegenerateReferralCodeUseCase {

    ReferralCodeDTO execute(String userId);
}
