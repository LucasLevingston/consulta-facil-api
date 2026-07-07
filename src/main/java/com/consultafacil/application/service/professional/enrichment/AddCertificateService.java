package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.enrichment.AddCertificateUseCase;
import com.consultafacil.domain.entity.ProfessionalCertificate;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.professional.enrichment.ProfessionalCertificateRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddCertificateService implements AddCertificateUseCase {

    private final ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    private final ProfessionalProfileByIdFinder profileByIdFinder;
    private final ProfessionalCertificateRepositoryPort certificateRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, ProfessionalCertificateDTO dto) {
        ProfessionalProfile profile = profileByUserIdFinder.findOrThrow(userId);
        ProfessionalCertificate certificate = ProfessionalCertificate.builder()
                .professionalProfile(profile)
                .title(dto.title())
                .issuingOrganization(dto.issuingOrganization())
                .issueYear(dto.issueYear())
                .certificateUrl(dto.certificateUrl())
                .build();
        certificateRepository.save(certificate);
        return mapper.toResponseDTO(profileByIdFinder.findOrThrow(profile.getId()));
    }
}
