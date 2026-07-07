package com.consultafacil.application.port.in.referral;

import com.consultafacil.api.dto.billing.referral.ReferralDTO;

import java.util.List;

public interface GetAllReferralsUseCase {

    List<ReferralDTO> execute();
}
