package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.exception.PaymentRequiredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequirePlanServiceTest {

    @Mock
    private GetEffectivePlanService getEffectivePlan;

    @InjectMocks
    private RequirePlanService service;

    @Test
    void freePlan_requiresProfessional_throws() {
        when(getEffectivePlan.execute("u1")).thenReturn(SubscriptionPlan.FREE);
        assertThrows(PaymentRequiredException.class,
                () -> service.execute("u1", SubscriptionPlan.PROFESSIONAL));
    }

    @Test
    void professionalPlan_requiresProfessional_passes() {
        when(getEffectivePlan.execute("u1")).thenReturn(SubscriptionPlan.PROFESSIONAL);
        assertDoesNotThrow(() -> service.execute("u1", SubscriptionPlan.PROFESSIONAL));
    }

    @Test
    void professionalPlan_requiresClinic_throws() {
        when(getEffectivePlan.execute("u1")).thenReturn(SubscriptionPlan.PROFESSIONAL);
        assertThrows(PaymentRequiredException.class,
                () -> service.execute("u1", SubscriptionPlan.CLINIC));
    }

    @Test
    void clinicPlan_requiresClinic_passes() {
        when(getEffectivePlan.execute("u1")).thenReturn(SubscriptionPlan.CLINIC);
        assertDoesNotThrow(() -> service.execute("u1", SubscriptionPlan.CLINIC));
    }
}
