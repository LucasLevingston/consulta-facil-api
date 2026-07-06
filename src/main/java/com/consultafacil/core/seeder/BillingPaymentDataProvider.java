package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.OwnerType;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.repository.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class BillingPaymentDataProvider {

    private final UserRepository userRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public record PaymentDef(String payerId, String payerName, String payerEmail,
            PaymentType type, OwnerType ownerType, String ownerId,
            BigDecimal amount, BigDecimal systemFee, String method,
            BillingPaymentStatus status, int daysAgo) {
    }

    private record PaymentTypeConfig(PaymentType type, BigDecimal fixedFee, double pctFee) {
    }

    public List<PaymentDef> build(String patientUserId, String professionalUserId, List<String> randomPatientIds) {
        List<String> methods = List.of("credit_card", "pix", "credit_card", "debit_card");
        List<PaymentTypeConfig> typeConfigs = List.of(
                new PaymentTypeConfig(PaymentType.CONSULTATION, new BigDecimal("5.00"), 0.00),
                new PaymentTypeConfig(PaymentType.PROCEDURE, new BigDecimal("10.00"), 0.02),
                new PaymentTypeConfig(PaymentType.EXAM, new BigDecimal("3.00"), 0.00),
                new PaymentTypeConfig(PaymentType.SUBSCRIPTION, BigDecimal.ZERO, 0.00));

        List<PaymentDef> defs = new ArrayList<>();
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.CONSULTATION, OwnerType.DOCTOR, professionalUserId,
                new BigDecimal("250.00"), new BigDecimal("5.00"), "credit_card", BillingPaymentStatus.PAID, 30));
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.EXAM, OwnerType.DOCTOR, professionalUserId,
                new BigDecimal("180.00"), new BigDecimal("3.00"), "pix", BillingPaymentStatus.PAID, 15));
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.PROCEDURE, OwnerType.DOCTOR, professionalUserId,
                new BigDecimal("350.00"), new BigDecimal("17.00"), "credit_card", BillingPaymentStatus.PAID, 7));
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.CONSULTATION, OwnerType.DOCTOR, professionalUserId,
                new BigDecimal("250.00"), new BigDecimal("5.00"), "pix", BillingPaymentStatus.PENDING, 1));
        defs.add(new PaymentDef(patientUserId, "Paciente Teste", "patient@example.com",
                PaymentType.SUBSCRIPTION, null, null,
                new BigDecimal("129.90"), BigDecimal.ZERO, "credit_card", BillingPaymentStatus.PAID, 45));

        addRandomPayments(defs, randomPatientIds, professionalUserId, methods, typeConfigs);
        return defs;
    }

    private void addRandomPayments(List<PaymentDef> defs, List<String> randomPatientIds, String professionalUserId,
            List<String> methods, List<PaymentTypeConfig> typeConfigs) {
        List<String> samplePatients = randomPatientIds.stream().limit(10).toList();
        List<BillingPaymentStatus> statusPool = List.of(
                BillingPaymentStatus.PAID, BillingPaymentStatus.PAID, BillingPaymentStatus.PAID,
                BillingPaymentStatus.PENDING, BillingPaymentStatus.FAILED, BillingPaymentStatus.CANCELED);

        for (String userId : samplePatients) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;
            int numPayments = 2 + faker.random().nextInt(4);
            for (int j = 0; j < numPayments; j++) {
                PaymentTypeConfig cfg = typeConfigs.get(faker.random().nextInt(typeConfigs.size() - 1));
                BigDecimal amount = BigDecimal.valueOf(100 + faker.random().nextInt(400));
                BigDecimal fee = cfg.fixedFee().add(amount.multiply(BigDecimal.valueOf(cfg.pctFee()))
                        .setScale(2, RoundingMode.HALF_UP));
                BillingPaymentStatus status = statusPool.get(faker.random().nextInt(statusPool.size()));
                defs.add(new PaymentDef(userId, user.getName(), user.getEmail(),
                        cfg.type(), OwnerType.DOCTOR, professionalUserId, amount, fee,
                        methods.get(faker.random().nextInt(methods.size())), status,
                        faker.random().nextInt(1, 120)));
            }
        }
    }
}
