package com.consultafacil.application.service.systemfee;

import com.consultafacil.api.dto.fees.FeeCalculationResponseDTO;
import com.consultafacil.api.dto.fees.FeeConfigDTO;
import com.consultafacil.api.dto.fees.PaymentMethodBreakdownDTO;
import com.consultafacil.application.port.in.systemfee.CalculateFeesUseCase;
import com.consultafacil.core.config.TaxConfig;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeCalculationService implements CalculateFeesUseCase {

    private static final BigDecimal DEFAULT_PLATFORM_RATE = new BigDecimal("0.0500");

    private final TaxConfig taxConfig;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final PlanRepositoryPort planRepository;

    @Override
    @Transactional(readOnly = true)
    public FeeConfigDTO getConfig(String userId) {
        BigDecimal platformRate = getPlatformFeeRate(userId);
        Plan plan = getActivePlan(userId);
        TaxConfig.ProcessingFee pf = taxConfig.getProcessingFee();

        return new FeeConfigDTO(
                toRate(pf.getPix()),
                toRate(pf.getCreditCard()),
                toRate(pf.getDebit()),
                platformRate,
                plan != null ? plan.getSlug() : "free",
                plan != null ? plan.getName() : "Free"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FeeCalculationResponseDTO calculate(BigDecimal amount, PaymentMethod paymentMethod,
                                               boolean professionalAbsorbsFees, String userId) {
        BigDecimal mpRate = getMpFeeRate(paymentMethod);
        BigDecimal platformRate = getPlatformFeeRate(userId);

        BigDecimal gross, net, mpFee, platformFee;

        if (professionalAbsorbsFees) {
            gross = amount;
            mpFee = gross.multiply(mpRate).setScale(2, RoundingMode.HALF_UP);
            platformFee = gross.multiply(platformRate).setScale(2, RoundingMode.HALF_UP);
            net = gross.subtract(mpFee).subtract(platformFee);
        } else {
            net = amount;
            BigDecimal divisor = BigDecimal.ONE.subtract(mpRate).subtract(platformRate);
            gross = net.divide(divisor, 2, RoundingMode.HALF_UP);
            mpFee = gross.multiply(mpRate).setScale(2, RoundingMode.HALF_UP);
            platformFee = gross.multiply(platformRate).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal baseForComparison = professionalAbsorbsFees ? gross : net;
        List<PaymentMethodBreakdownDTO> comparison = buildComparison(baseForComparison, platformRate, professionalAbsorbsFees);

        return new FeeCalculationResponseDTO(
                amount,
                paymentMethod.name(),
                mpRate,
                mpFee,
                platformRate,
                platformFee,
                mpFee.add(platformFee),
                net,
                gross,
                comparison
        );
    }

    private BigDecimal getMpFeeRate(PaymentMethod method) {
        TaxConfig.ProcessingFee pf = taxConfig.getProcessingFee();
        return switch (method) {
            case PIX -> toRate(pf.getPix());
            case CREDIT_CARD, MERCADOPAGO -> toRate(pf.getCreditCard());
            case DEBIT_CARD -> toRate(pf.getDebit());
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal getPlatformFeeRate(String userId) {
        Plan plan = getActivePlan(userId);
        if (plan == null || plan.getConsultationFeeRate() == null) {
            return DEFAULT_PLATFORM_RATE;
        }
        return plan.getConsultationFeeRate();
    }

    private Plan getActivePlan(String userId) {
        return subscriptionRepository.findByUserId(userId)
                .flatMap(sub -> planRepository.findById(sub.getPlanId()))
                .orElse(null);
    }

    private List<PaymentMethodBreakdownDTO> buildComparison(BigDecimal base, BigDecimal platformRate,
                                                            boolean profAbsorbs) {
        return Arrays.stream(PaymentMethod.values())
                .map(m -> {
                    BigDecimal mp = getMpFeeRate(m);
                    BigDecimal gross, net, mpFee, platformFee;
                    if (profAbsorbs) {
                        gross = base;
                        mpFee = gross.multiply(mp).setScale(2, RoundingMode.HALF_UP);
                        platformFee = gross.multiply(platformRate).setScale(2, RoundingMode.HALF_UP);
                        net = gross.subtract(mpFee).subtract(platformFee);
                    } else {
                        net = base;
                        BigDecimal divisor = BigDecimal.ONE.subtract(mp).subtract(platformRate);
                        gross = net.divide(divisor, 2, RoundingMode.HALF_UP);
                        mpFee = gross.multiply(mp).setScale(2, RoundingMode.HALF_UP);
                        platformFee = gross.multiply(platformRate).setScale(2, RoundingMode.HALF_UP);
                    }
                    return new PaymentMethodBreakdownDTO(m.name(), mp, mpFee, platformFee,
                            mpFee.add(platformFee), net, gross);
                })
                .toList();
    }

    private BigDecimal toRate(BigDecimal percentage) {
        return percentage.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }
}
