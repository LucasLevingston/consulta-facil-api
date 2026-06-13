package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateSocialLinksDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProfessionalProfileUseCase {

    ProfessionalResponseDTO createProfile(String userId, CreateProfessionalDTO dto);

    ProfessionalResponseDTO getById(String professionalId);

    ProfessionalResponseDTO getByUserId(String userId);

    Page<ProfessionalResponseDTO> searchBySpecialty(String specialty, Pageable pageable);

    Page<ProfessionalResponseDTO> getAll(String profession, String specialty, String name, Pageable pageable);

    List<ProfessionalResponseDTO> getNearby(double lat, double lng, double radiusKm,
                                             String specialty, String profession);

    Page<ProfessionalResponseDTO> getPendingApplications(Pageable pageable);

    ProfessionalResponseDTO getApplicationStatus(String userId);

    ProfessionalResponseDTO approveApplication(String professionalId);

    ProfessionalResponseDTO rejectApplication(String professionalId);

    ProfessionalResponseDTO updateProfessional(String professionalId, CreateProfessionalDTO dto);

    void deleteProfessional(String professionalId);

    ProfessionalResponseDTO updateSocialLinks(String userId, UpdateSocialLinksDTO dto);
}
