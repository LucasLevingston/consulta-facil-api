package com.consultafacil.application.service.subscription;

import com.consultafacil.api.dto.subscription.SubscriptionPlanInfoDTO;
import com.consultafacil.application.port.in.subscription.ListSubscriptionPlansUseCase;
import com.consultafacil.domain.enums.SubscriptionPlan;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ListSubscriptionPlansService implements ListSubscriptionPlansUseCase {

    @Override
    public List<SubscriptionPlanInfoDTO> execute() {
        return List.of(
            SubscriptionPlanInfoDTO.builder()
                .plan(SubscriptionPlan.FREE)
                .name("Gratuito")
                .monthlyPrice(BigDecimal.ZERO)
                .features(List.of("Agenda online", "Perfil profissional", "App mobile", "Até 10 consultas por mês"))
                .maxAppointmentsPerMonth(10)
                .build(),
            SubscriptionPlanInfoDTO.builder()
                .plan(SubscriptionPlan.PROFESSIONAL)
                .name("Profissional")
                .monthlyPrice(new BigDecimal("79.00"))
                .features(List.of("Tudo do Gratuito", "Consultas ilimitadas", "Telemedicina (online)", "Analytics", "Notificações por e-mail"))
                .maxAppointmentsPerMonth(-1)
                .build(),
            SubscriptionPlanInfoDTO.builder()
                .plan(SubscriptionPlan.CLINIC)
                .name("Clínica")
                .monthlyPrice(new BigDecimal("199.00"))
                .features(List.of("Tudo do Profissional", "Múltiplos profissionais", "Recepcionista", "Dashboard administrativo", "Branding personalizado"))
                .maxAppointmentsPerMonth(-1)
                .build()
        );
    }
}
