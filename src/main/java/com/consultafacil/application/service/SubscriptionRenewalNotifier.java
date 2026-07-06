package com.consultafacil.application.service;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionRenewalNotifier {

    private final EmailPort emailPort;

    public void sendRenewalEmail(User user, Plan plan, LocalDateTime nextExpiry) {
        try {
            String nextDate = nextExpiry.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")));
            emailPort.sendSubscriptionRenewed(
                    user.getEmail(), user.getName(), plan.getName(),
                    plan.getPrice().toPlainString(), nextDate);
        } catch (Exception e) {
            log.error("[Email] Failed to send renewal email for user {}: {}", user.getId(), e.getMessage());
        }
    }
}
