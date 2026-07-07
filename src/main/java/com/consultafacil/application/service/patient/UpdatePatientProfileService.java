package com.consultafacil.application.service.patient;

import com.consultafacil.application.port.in.patient.UpdatePatientProfileUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UpdatePatientProfileService implements UpdatePatientProfileUseCase {

    private final PatientProfileRepositoryPort patientProfileRepository;
    private final PatientProfileMapper mapper;

    @Override
    @Transactional
    public Map<String, Object> execute(String userId, Map<String, Object> updates) {
        PatientProfile patientProfile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        if (updates.containsKey("occupation")) {
            patientProfile.setOccupation((String) updates.get("occupation"));
        }

        PatientProfile updated = patientProfileRepository.save(patientProfile);
        User user = updated.getUser();

        return mapper.toResponseMap(user, updated);
    }
}
