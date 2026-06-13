package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.referral.ReferralCodeDTO;
import com.consultafacil.api.dto.billing.referral.ReferralDTO;
import com.consultafacil.api.dto.billing.referral.ReferralStatsDTO;

import java.util.List;

public interface ReferralUseCase {
    ReferralCodeDTO getOrCreateReferralCode(String userId);
    ReferralCodeDTO regenerateCode(String userId);
    void registerReferral(String referredId, String code);
    ReferralStatsDTO getUserReferralStats(String userId);
    List<ReferralDTO> getAllReferrals();
}
