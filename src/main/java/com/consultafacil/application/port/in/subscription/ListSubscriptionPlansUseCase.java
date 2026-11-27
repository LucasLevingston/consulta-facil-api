package com.consultafacil.application.port.in.subscription;

import com.consultafacil.api.dto.subscription.SubscriptionPlanInfoDTO;
import java.util.List;

public interface ListSubscriptionPlansUseCase {
    List<SubscriptionPlanInfoDTO> execute();
}
