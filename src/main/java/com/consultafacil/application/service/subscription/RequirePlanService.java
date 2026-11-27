package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.exception.PaymentRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequirePlanService {

    private final GetEffectivePlanService getEffectivePlan;

    public void execute(String userId, SubscriptionPlan required) {
        SubscriptionPlan effective = getEffectivePlan.execute(userId);
        if (effective.ordinal() < required.ordinal()) {
            throw new PaymentRequiredException(required.name());
        }
    }
}
