package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;
import com.consultafacil.api.dto.professional.UpdateCouncilDTO;

public interface ProfessionalEnrichmentUseCase {

    ProfessionalResponseDTO updateCouncil(String userId, UpdateCouncilDTO dto);

    ProfessionalResponseDTO updateAddress(String userId, UpdateAddressDTO dto);

    ProfessionalResponseDTO addEducation(String userId, ProfessionalEducationDTO dto);

    ProfessionalResponseDTO updateEducation(String userId, String educationId, ProfessionalEducationDTO dto);

    ProfessionalResponseDTO deleteEducation(String userId, String educationId);

    ProfessionalResponseDTO addExperience(String userId, ProfessionalExperienceDTO dto);

    ProfessionalResponseDTO updateExperience(String userId, String experienceId, ProfessionalExperienceDTO dto);

    ProfessionalResponseDTO deleteExperience(String userId, String experienceId);

    ProfessionalResponseDTO addCertificate(String userId, ProfessionalCertificateDTO dto);

    ProfessionalResponseDTO updateCertificate(String userId, String certificateId, ProfessionalCertificateDTO dto);

    ProfessionalResponseDTO deleteCertificate(String userId, String certificateId);
}
