package com.consultafacil.domain.port.out.clinic;

import com.consultafacil.domain.entity.Clinic;

import java.util.List;
import java.util.Optional;

public interface ClinicRepositoryPort {

    Clinic save(Clinic clinic);

    Clinic saveAndFlush(Clinic clinic);

    Optional<Clinic> findById(String id);

    List<Clinic> findByOwnerId(String ownerId);

    List<Clinic> findByStatus(String status);

    Optional<Clinic> findByIdWithMembers(String id);

    List<Clinic> findNearby(double lat, double lng, double radiusKm);
}
