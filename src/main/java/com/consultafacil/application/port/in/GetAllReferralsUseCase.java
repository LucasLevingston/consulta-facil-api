package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.referral.ReferralDTO;

import java.util.List;

public interface GetAllReferralsUseCase {

    List<ReferralDTO> execute();
}
