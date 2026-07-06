package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.CreateProfessionalDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateBioDTO;
import com.consultafacil.api.dto.professional.UpdateSocialLinksDTO;
import com.consultafacil.application.port.in.ProfessionalProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionalService implements ProfessionalProfileUseCase {

    private final ProfessionalProfileQueryService queryService;
    private final ProfessionalProfileCommandService commandService;
    private final ProfessionalProfileAdminCommandService adminCommandService;

    @Override
    public ProfessionalResponseDTO createProfile(String userId, CreateProfessionalDTO dto) {
        return commandService.createProfessionalProfile(userId, dto);
    }

    @Override
    public ProfessionalResponseDTO getById(String professionalId) {
        return queryService.getProfessionalById(professionalId);
    }

    @Override
    public ProfessionalResponseDTO getByUserId(String userId) {
        return queryService.getProfessionalByUserId(userId);
    }

    @Override
    public Page<ProfessionalResponseDTO> searchBySpecialty(String specialty, Pageable pageable) {
        return queryService.searchBySpecialty(specialty, pageable);
    }

    @Override
    public Page<ProfessionalResponseDTO> getAll(String profession, String specialty, String name, Pageable pageable) {
        return queryService.getAllProfessionals(profession, specialty, name, pageable);
    }

    @Override
    public List<ProfessionalResponseDTO> getNearby(double lat, double lng, double radiusKm,
                                                    String specialty, String profession) {
        return queryService.getProfessionalsNearby(lat, lng, radiusKm, specialty, profession);
    }

    @Override
    public Page<ProfessionalResponseDTO> getPendingApplications(Pageable pageable) {
        return queryService.getPendingApplications(pageable);
    }

    @Override
    public ProfessionalResponseDTO getApplicationStatus(String userId) {
        return queryService.getApplicationStatus(userId);
    }

    @Override
    public ProfessionalResponseDTO approveApplication(String professionalId) {
        return commandService.approveApplication(professionalId);
    }

    @Override
    public ProfessionalResponseDTO rejectApplication(String professionalId) {
        return commandService.rejectApplication(professionalId);
    }

    @Override
    public ProfessionalResponseDTO updateProfessional(String professionalId, CreateProfessionalDTO dto) {
        return adminCommandService.updateProfessional(professionalId, dto);
    }

    @Override
    public void deleteProfessional(String professionalId) {
        adminCommandService.deleteProfessional(professionalId);
    }

    @Override
    public ProfessionalResponseDTO updateSocialLinks(String userId, UpdateSocialLinksDTO dto) {
        return commandService.updateSocialLinks(userId, dto);
    }

    @Override
    public ProfessionalResponseDTO updateBio(String userId, UpdateBioDTO dto) {
        return commandService.updateBio(userId, dto);
    }
}
