package com.consultafacil.application.service.patient;

import com.consultafacil.application.port.in.GetPatientMedicalRecordsUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.MedicalRecord;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPatientMedicalRecordsService implements GetPatientMedicalRecordsUseCase {

    private final PatientProfileRepositoryPort patientProfileRepository;
    private final MedicalRecordMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> execute(String userId) {
        log.debug("Fetching medical records for user: {}", userId);

        PatientProfile patientProfile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        MedicalRecord medicalRecord = patientProfile.getMedicalRecord();

        if (medicalRecord == null) {
            throw new ResourceNotFoundException("Medical records not found for this patient");
        }

        return mapper.toResponseMap(medicalRecord);
    }
}
