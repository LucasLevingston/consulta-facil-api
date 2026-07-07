package com.consultafacil.domain.port.out.professional.profile;

import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Specialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

// NOTE: Page/Pageable are Spring types — pragmatic compromise for pagination support.
// A pure port would use custom pagination types, but that requires a larger refactor.
public interface ProfessionalProfileRepositoryPort {

    ProfessionalProfile save(ProfessionalProfile profile);

    Optional<ProfessionalProfile> findById(String id);

    Optional<ProfessionalProfile> findByUserId(String userId);

    boolean existsByLicenseNumber(String licenseNumber);

    void delete(ProfessionalProfile profile);

    Page<ProfessionalProfile> findByStatus(ProfessionalProfileStatus status, Pageable pageable);

    Page<ProfessionalProfile> findBySpecialtyAndStatus(
            Specialty specialty, ProfessionalProfileStatus status, Pageable pageable);

    Page<ProfessionalProfile> findActiveWithFilters(
            String profession, String specialty, String name, Pageable pageable);

    List<ProfessionalProfile> findNearby(
            double lat, double lng, double radiusKm, String specialty, String profession);
}
