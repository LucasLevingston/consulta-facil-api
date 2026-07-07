package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.application.port.in.patient.ListVaccinesUseCase;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.patient.PatientVaccineRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListVaccinesService implements ListVaccinesUseCase {

    private final PatientProfileFinder profileFinder;
    private final PatientVaccineRepositoryPort vaccineRepository;
    private final PatientVaccineMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PatientVaccineDTO> execute(String userId) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        return vaccineRepository.findByPatientProfileId(profile.getId())
                .stream().map(mapper::toDTO).toList();
    }
}
