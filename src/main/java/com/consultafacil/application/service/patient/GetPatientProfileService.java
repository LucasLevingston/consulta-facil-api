package com.consultafacil.application.service.patient;

import com.consultafacil.application.port.in.GetPatientProfileUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPatientProfileService implements GetPatientProfileUseCase {

    private final PatientProfileRepositoryPort patientProfileRepository;
    private final UserRepositoryPort userRepository;
    private final PatientProfileMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> execute(String userId) {
        log.debug("Fetching patient profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        PatientProfile patientProfile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        return mapper.toResponseMap(user, patientProfile);
    }
}
