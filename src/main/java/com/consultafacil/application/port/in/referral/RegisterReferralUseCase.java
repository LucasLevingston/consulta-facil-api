package com.consultafacil.application.port.in.referral;

public interface RegisterReferralUseCase {

    void execute(String referredId, String code);
}
