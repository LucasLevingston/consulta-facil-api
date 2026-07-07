package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanSeeder {

    private final PlanRepository planRepository;

    private record PlanDef(String slug, String name, String tier, BillingPeriod period,
            BigDecimal price, String features, int order) {
    }

    public void seed() {
        List<PlanDef> defs = List.of(
                new PlanDef("plan_basic", "Básico", "basic", BillingPeriod.MONTHLY, new BigDecimal("49.90"),
                        "Até 50 consultas/mês;Agenda online;Suporte por email", 1),
                new PlanDef("plan_pro", "Pro", "pro", BillingPeriod.MONTHLY, new BigDecimal("129.90"),
                        "Consultas ilimitadas;Teleconsulta;Prontuário digital;Suporte prioritário", 2),
                new PlanDef("plan_premium", "Premium", "premium", BillingPeriod.MONTHLY, new BigDecimal("249.90"),
                        "Tudo do Pro;IA assistente;Gestão financeira;API de integração;Suporte 24/7", 3),
                new PlanDef("plan_pro_annual", "Pro Anual", "pro", BillingPeriod.ANNUAL, new BigDecimal("1199.90"),
                        "Tudo do Pro;2 meses grátis;Suporte dedicado", 4));

        for (PlanDef def : defs) {
            try {
                if (planRepository.findBySlug(def.slug()).isEmpty()) {
                    planRepository.save(Plan.builder()
                            .slug(def.slug())
                            .name(def.name())
                            .tier(def.tier())
                            .billingPeriod(def.period())
                            .price(def.price())
                            .features(def.features())
                            .status(PlanStatus.ACTIVE)
                            .displayOrder(def.order())
                            .build());
                }
            } catch (Exception e) {
                log.warn("Erro ao criar plano {}: {}", def.slug(), e.getMessage());
            }
        }
        log.info("[Seed] Planos criados: {}", defs.size());
    }
}
