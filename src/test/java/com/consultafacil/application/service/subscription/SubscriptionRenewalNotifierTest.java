package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.EmailPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionRenewalNotifierTest {

    @Mock EmailPort emailPort;

    private final User user = User.builder().id("u1").email("ana@example.com").name("Ana").build();
    private final Plan plan = Plan.builder().slug("monthly").name("Plano Pro Mensal").price(new BigDecimal("79.90")).build();

    @Test
    void sendRenewalEmail_formatsNextBillingDateInBrazilianFormat() {
        new SubscriptionRenewalNotifier(emailPort).sendRenewalEmail(user, plan, LocalDateTime.of(2026, 12, 5, 10, 0));

        verify(emailPort).sendSubscriptionRenewed("ana@example.com", "Ana", "Plano Pro Mensal", "79.90", "05/12/2026");
    }

    @Test
    void sendRenewalEmail_swallowsEmailFailures() {
        doThrow(new RuntimeException("SES down")).when(emailPort)
                .sendSubscriptionRenewed(any(), any(), any(), any(), any());

        assertThatCode(() -> new SubscriptionRenewalNotifier(emailPort)
                .sendRenewalEmail(user, plan, LocalDateTime.of(2026, 12, 5, 10, 0)))
                .doesNotThrowAnyException();
    }
}
