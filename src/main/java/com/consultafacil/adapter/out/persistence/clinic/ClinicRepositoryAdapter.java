package com.consultafacil.adapter.out.persistence.clinic;

import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.port.out.clinic.ClinicRepositoryPort;
import com.consultafacil.domain.repository.clinic.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClinicRepositoryAdapter implements ClinicRepositoryPort {

    private final ClinicRepository clinicRepository;

    @Override
    public Clinic save(Clinic clinic) {
        return clinicRepository.save(clinic);
    }

    @Override
    public Clinic saveAndFlush(Clinic clinic) {
        return clinicRepository.saveAndFlush(clinic);
    }

    @Override
    public Optional<Clinic> findById(String id) {
        return clinicRepository.findById(id);
    }

    @Override
    public List<Clinic> findByOwnerId(String ownerId) {
        return clinicRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Clinic> findByStatus(String status) {
        return clinicRepository.findByStatus(status);
    }

    @Override
    public Optional<Clinic> findByIdWithMembers(String id) {
        return clinicRepository.findByIdWithMembers(id);
    }

    @Override
    public List<Clinic> findNearby(double lat, double lng, double radiusKm) {
        return clinicRepository.findNearby(lat, lng, radiusKm);
    }
}
