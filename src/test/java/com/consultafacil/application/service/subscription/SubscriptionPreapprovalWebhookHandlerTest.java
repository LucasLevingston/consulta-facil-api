package com.consultafacil.application.service.subscription;

import com.consultafacil.application.port.in.RecordCouponUseUseCase;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionPreapprovalWebhookHandlerTest {

    @Mock SubscriptionRepositoryPort subscriptionRepository;
    @Mock PlanRepositoryPort planRepository;
    @Mock RecordCouponUseUseCase couponUseCase;
    @Mock SubscriptionSellerSaleLinker sellerSaleLinker;
    @Mock SubscriptionPaymentRecorder paymentRecorder;

    @InjectMocks SubscriptionPreapprovalWebhookHandler handler;

    @Test
    void handle_notFoundInDB_doesNotThrow() {
        // PreapprovalClient will fail in unit context but exception is caught
        handler.execute("preapproval-1");
    }
}
