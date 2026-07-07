package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.repository.subscription.SubscriptionPaymentRepository;
import com.consultafacil.domain.repository.subscription.SubscriptionRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionPaymentSeeder {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public void seed() {
        List<Subscription> active = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();

        List<String> methods = List.of("credit_card", "pix", "boleto");
        int created = 0;

        for (Subscription sub : active) {
            int payments = faker.random().nextInt(1, 4);
            for (int i = 0; i < payments; i++) {
                try {
                    BigDecimal gross = BigDecimal.valueOf(49.90 + faker.random().nextInt(200));
                    BigDecimal fee = gross.multiply(new BigDecimal("0.039")).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal net = gross.subtract(fee);
                    subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                            .subscriptionId(sub.getId())
                            .mpPaymentId("mp_" + faker.random().nextInt(100000, 999999))
                            .grossAmount(gross)
                            .processingFee(fee)
                            .netAmount(net)
                            .paymentMethod(methods.get(faker.random().nextInt(methods.size())))
                            .paidAt(LocalDateTime.now().minusDays(i * 30L + faker.random().nextInt(5)))
                            .build());
                    created++;
                } catch (Exception e) {
                    log.debug("Erro ao criar subscription payment: {}", e.getMessage());
                }
            }
        }
        log.info("[Seed] SubscriptionPayments criados: {}", created);
    }
}
