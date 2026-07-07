package com.consultafacil.application.service.patient;

import com.consultafacil.application.port.in.GetAllPatientsUseCase;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetAllPatientsService implements GetAllPatientsUseCase {

    private final PatientProfileRepositoryPort patientProfileRepository;
    private final PatientProfileMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> execute(Pageable pageable) {
        return patientProfileRepository.findAll(pageable)
                .map(pp -> mapper.toResponseMap(pp.getUser(), pp));
    }
}
