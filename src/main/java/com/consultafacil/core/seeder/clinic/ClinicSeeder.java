package com.consultafacil.core.seeder.clinic;

import com.consultafacil.api.dto.clinic.CreateClinicDTO;
import com.consultafacil.application.port.in.clinic.AddClinicMemberUseCase;
import com.consultafacil.application.port.in.clinic.CreateClinicUseCase;
import com.consultafacil.domain.repository.professional.profile.ProfessionalProfileRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClinicSeeder {

    private final ProfessionalProfileRepository professionalProfileRepository;
    private final CreateClinicUseCase createClinicUseCase;
    private final AddClinicMemberUseCase addClinicMemberUseCase;
    private final ClinicDataProvider clinicDataProvider;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    public String seed(String testProfessionalProfileId, String professionalUserId, List<String> extraIds) {
        var defs = clinicDataProvider.buildDefs(testProfessionalProfileId, professionalUserId, extraIds);
        String firstClinicId = null;

        for (var def : defs) {
            try {
                professionalProfileRepository.findById(def.ownerProfileId()).ifPresent(p -> {
                    p.setCity(def.location().city());
                    p.setState(def.location().state());
                    p.setLatitude(def.location().lat() + (faker.random().nextDouble() * 0.02 - 0.01));
                    p.setLongitude(def.location().lng() + (faker.random().nextDouble() * 0.02 - 0.01));
                    professionalProfileRepository.save(p);
                });

                String ownerUserId = professionalProfileRepository.findById(def.ownerProfileId())
                        .map(p -> p.getUser().getId())
                        .orElse(null);
                if (ownerUserId == null) continue;

                CreateClinicDTO dto = new CreateClinicDTO();
                dto.setName(def.name());
                dto.setDescription(def.description());
                dto.setPhone(def.phone());
                dto.setAddress(def.address());
                dto.setCity(def.location().city());
                dto.setState(def.location().state());
                dto.setLatitude(def.location().lat());
                dto.setLongitude(def.location().lng());
                dto.setImageUrl(def.imageUrl());

                var clinic = createClinicUseCase.execute(ownerUserId, dto);
                if (firstClinicId == null) firstClinicId = clinic.getId();

                addExtraMembers(clinic.getId(), def, extraIds, ownerUserId);
                log.info("Clínica criada: {} ({}, {})", clinic.getName(), def.location().city(), def.location().state());
            } catch (Exception e) {
                log.warn("Erro ao criar clínica {}: {}", def.name(), e.getMessage());
            }
        }
        return firstClinicId;
    }

    private void addExtraMembers(String clinicId, ClinicDataProvider.ClinicDef def,
            List<String> extraIds, String ownerUserId) {
        int added = 0;
        for (String extraId : extraIds) {
            if (added >= 2) break;
            if (extraId.equals(def.ownerProfileId())) continue;
            try {
                addClinicMemberUseCase.execute(clinicId, extraId, ownerUserId);
                professionalProfileRepository.findById(extraId).ifPresent(p -> {
                    if (p.getLatitude() == null) {
                        p.setCity(def.location().city());
                        p.setState(def.location().state());
                        p.setLatitude(def.location().lat() + (faker.random().nextDouble() * 0.04 - 0.02));
                        p.setLongitude(def.location().lng() + (faker.random().nextDouble() * 0.04 - 0.02));
                        professionalProfileRepository.save(p);
                    }
                });
                added++;
            } catch (Exception ignored) {
            }
        }
    }
}
