package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.ClinicReceptionist;
import com.consultafacil.domain.port.out.ClinicReceptionistRepositoryPort;
import com.consultafacil.domain.repository.ClinicReceptionistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClinicReceptionistRepositoryAdapter implements ClinicReceptionistRepositoryPort {

    private final ClinicReceptionistRepository clinicReceptionistRepository;

    @Override
    public ClinicReceptionist save(ClinicReceptionist receptionist) {
        return clinicReceptionistRepository.save(receptionist);
    }

    @Override
    public Optional<ClinicReceptionist> findById(String id) {
        return clinicReceptionistRepository.findById(id);
    }

    @Override
    public List<ClinicReceptionist> findByClinicId(String clinicId) {
        return clinicReceptionistRepository.findByClinicId(clinicId);
    }

    @Override
    public Optional<ClinicReceptionist> findByClinicIdAndUserId(String clinicId, String userId) {
        return clinicReceptionistRepository.findByClinicIdAndUserId(clinicId, userId);
    }

    @Override
    public boolean existsByClinicIdAndUserId(String clinicId, String userId) {
        return clinicReceptionistRepository.existsByClinicIdAndUserId(clinicId, userId);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return clinicReceptionistRepository.existsByUserId(userId);
    }

    @Override
    public void delete(ClinicReceptionist receptionist) {
        clinicReceptionistRepository.delete(receptionist);
    }
}
