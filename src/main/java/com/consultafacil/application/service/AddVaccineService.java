package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.application.port.in.AddVaccineUseCase;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.PatientVaccine;
import com.consultafacil.domain.port.out.PatientVaccineRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddVaccineService implements AddVaccineUseCase {

    private final PatientProfileFinder profileFinder;
    private final PatientVaccineRepositoryPort vaccineRepository;
    private final PatientVaccineMapper mapper;

    @Override
    @Transactional
    public PatientVaccineDTO execute(String userId, PatientVaccineDTO dto) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        PatientVaccine vaccine = PatientVaccine.builder()
                .vaccineName(dto.vaccineName())
                .doseNumber(dto.doseNumber())
                .administeredAt(dto.administeredAt())
                .notes(dto.notes())
                .patientProfile(profile)
                .build();
        return mapper.toDTO(vaccineRepository.save(vaccine));
    }
}
