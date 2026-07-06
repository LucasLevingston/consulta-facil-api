package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.repository.ProfessionalProfileRepository;
import com.consultafacil.domain.repository.SubscriptionRepository;
import com.consultafacil.domain.repository.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionSeeder {

    private final UserRepository userRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed(String testProfessionalUserId, List<String> professionalProfileIds) {
        List<String> planIds = List.of("plan_basic", "plan_pro", "plan_premium");
        List<SubscriptionStatus> statuses = List.of(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.ACTIVE, SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING, SubscriptionStatus.CANCELLED);

        try {
            userRepository.findById(testProfessionalUserId).ifPresent(user -> {
                if (subscriptionRepository.findByUserId(user.getId()).isPresent()) return;
                subscriptionRepository.save(Subscription.builder()
                        .user(user)
                        .planId("plan_pro")
                        .status(SubscriptionStatus.ACTIVE)
                        .expiresAt(LocalDateTime.now().plusMonths(6))
                        .build());
            });
        } catch (Exception e) {
            log.debug("Erro ao criar subscription teste: {}", e.getMessage());
        }

        int created = 0;
        for (int i = 0; i < Math.min(professionalProfileIds.size(), 20); i++) {
            final String profId = professionalProfileIds.get(i);
            try {
                professionalProfileRepository.findById(profId).ifPresent(prof -> {
                    String userId = prof.getUser().getId();
                    if (subscriptionRepository.findByUserId(userId).isPresent()) return;
                    SubscriptionStatus status = statuses.get(faker.random().nextInt(statuses.size()));
                    subscriptionRepository.save(Subscription.builder()
                            .user(prof.getUser())
                            .planId(planIds.get(faker.random().nextInt(planIds.size())))
                            .status(status)
                            .expiresAt(status == SubscriptionStatus.ACTIVE
                                    ? LocalDateTime.now().plusMonths(faker.random().nextInt(1, 12))
                                    : null)
                            .build());
                });
                created++;
            } catch (Exception e) {
                log.debug("Erro ao criar subscription: {}", e.getMessage());
            }
        }
        log.info("[Seed] Subscriptions criadas: {}", created);
    }
}
