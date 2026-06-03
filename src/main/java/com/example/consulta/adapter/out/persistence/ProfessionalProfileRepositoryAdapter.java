package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfessionalProfileRepositoryAdapter implements ProfessionalProfileRepositoryPort {

    private final ProfessionalProfileRepository professionalProfileRepository;

    @Override
    public ProfessionalProfile save(ProfessionalProfile profile) {
        return professionalProfileRepository.save(profile);
    }

    @Override
    public Optional<ProfessionalProfile> findById(String id) {
        return professionalProfileRepository.findById(id);
    }

    @Override
    public Optional<ProfessionalProfile> findByUserId(String userId) {
        return professionalProfileRepository.findByUserId(userId);
    }

    @Override
    public boolean existsByLicenseNumber(String licenseNumber) {
        return professionalProfileRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    public void delete(ProfessionalProfile profile) {
        professionalProfileRepository.delete(profile);
    }

    @Override
    public Page<ProfessionalProfile> findByStatus(ProfessionalProfileStatus status, Pageable pageable) {
        return professionalProfileRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<ProfessionalProfile> findBySpecialtyContainingIgnoreCaseAndStatus(
            String specialty, ProfessionalProfileStatus status, Pageable pageable) {
        return professionalProfileRepository.findBySpecialtyContainingIgnoreCaseAndStatus(specialty, status, pageable);
    }

    @Override
    public Page<ProfessionalProfile> findActiveWithFilters(
            String profession, String specialty, String name, Pageable pageable) {
        return professionalProfileRepository.findActiveWithFilters(profession, specialty, name, pageable);
    }

    @Override
    public List<ProfessionalProfile> findNearby(
            double lat, double lng, double radiusKm, String specialty, String profession) {
        return professionalProfileRepository.findNearby(lat, lng, radiusKm, specialty, profession);
    }
}
