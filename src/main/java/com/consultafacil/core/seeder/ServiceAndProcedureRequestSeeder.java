package com.consultafacil.core.seeder;

import com.consultafacil.api.dto.procedurerequest.CreateProcedureRequestDTO;
import com.consultafacil.api.dto.professionalservice.CreateProfessionalServiceDTO;
import com.consultafacil.application.service.procedurerequest.CreateProcedureRequestService;
import com.consultafacil.application.service.professional.service.CreateProfessionalServiceService;
import com.consultafacil.application.service.professional.profile.SetConsultationPriceService;
import com.consultafacil.domain.repository.patient.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceAndProcedureRequestSeeder {

    private final SetConsultationPriceService setConsultationPriceService;
    private final CreateProfessionalServiceService createProfessionalServiceService;
    private final CreateProcedureRequestService createProcedureRequestService;
    private final PatientProfileRepository patientProfileRepository;

    private record ServiceDef(String name, String description, BigDecimal price, int duration,
            boolean requiresConsultation) {
    }

    public void seed(String professionalUserId, String patientUserId) {
        if (professionalUserId == null) return;

        try {
            setConsultationPriceService.execute(professionalUserId, new BigDecimal("250.00"));
        } catch (Exception e) {
            log.warn("Erro ao definir preço de consulta no seed: {}", e.getMessage());
        }

        List<ServiceDef> services = List.of(
                new ServiceDef("Consulta de Cardiologia", "Consulta clínica de cardiologia",
                        new BigDecimal("250.00"), 30, false),
                new ServiceDef("ECG - Eletrocardiograma", "Exame do ritmo cardíaco em repouso",
                        new BigDecimal("180.00"), 20, false),
                new ServiceDef("Holter 24h", "Monitoramento cardíaco contínuo de 24 horas",
                        new BigDecimal("350.00"), 60, true),
                new ServiceDef("Ecocardiograma", "Ultrassom do coração com avaliação funcional",
                        new BigDecimal("450.00"), 60, true),
                new ServiceDef("MAPA", "Monitoramento ambulatorial da pressão arterial",
                        new BigDecimal("320.00"), 45, true));

        List<String> requiresConsultationServiceIds = new ArrayList<>();
        for (ServiceDef def : services) {
            try {
                var dto = CreateProfessionalServiceDTO.builder()
                        .name(def.name()).description(def.description()).price(def.price())
                        .durationMinutes(def.duration()).requiresConsultation(def.requiresConsultation())
                        .build();
                var created = createProfessionalServiceService.execute(professionalUserId, dto);
                if (def.requiresConsultation()) requiresConsultationServiceIds.add(created.getId());
                log.info("Serviço criado no seed: {}", def.name());
            } catch (Exception e) {
                log.warn("Erro ao criar serviço no seed: {}", e.getMessage());
            }
        }

        patientProfileRepository.findByUserId(patientUserId).ifPresent(patientProfile -> {
            for (String serviceId : requiresConsultationServiceIds) {
                try {
                    var dto = CreateProcedureRequestDTO.builder()
                            .serviceId(serviceId)
                            .patientId(patientProfile.getId())
                            .notes("Paciente encaminhado para avaliação. Aguarda agendamento.")
                            .build();
                    createProcedureRequestService.execute(professionalUserId, dto);
                } catch (Exception e) {
                    log.warn("Erro ao criar procedure request no seed: {}", e.getMessage());
                }
            }
        });
    }
}
