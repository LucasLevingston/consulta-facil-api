package com.consultafacil.application.service.patient;

import com.consultafacil.application.port.in.patient.DeleteVaccineUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.PatientVaccine;
import com.consultafacil.domain.port.out.patient.PatientVaccineRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteVaccineService implements DeleteVaccineUseCase {

    private final PatientProfileFinder profileFinder;
    private final PatientOwnershipGuard ownershipGuard;
    private final PatientVaccineRepositoryPort vaccineRepository;

    @Override
    @Transactional
    public void execute(String userId, String vaccineId) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        PatientVaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientVaccine", vaccineId));
        ownershipGuard.assertOwnership(vaccine.getPatientProfile().getId(), profile.getId());
        vaccineRepository.delete(vaccine);
    }
}
