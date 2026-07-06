package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ProfessionalProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProfessionalProfileMapper {

    private final ProfessionalRatingCalculator ratingCalculator;

    public ProfessionalResponseDTO toResponseDTO(ProfessionalProfile profile) {
        Clinic clinic = profile.getClinicMemberships().stream()
                .findFirst()
                .map(m -> m.getClinic())
                .orElse(null);

        return ProfessionalResponseDTO.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .profession(profile.getProfession() != null ? profile.getProfession().name() : null)
                .specialty(profile.getSpecialty() != null ? profile.getSpecialty().name() : null)
                .licenseNumber(profile.getLicenseNumber())
                .phone(profile.getUser().getPhone())
                .imageUrl(profile.getUser().getImageUrl())
                .rating(ratingCalculator.computeRating(profile))
                .consultationCount(ratingCalculator.computeConsultationCount(profile))
                .status(profile.getStatus())
                .city(profile.getCity())
                .state(profile.getState())
                .address(profile.getAddress())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .clinicId(clinic != null ? clinic.getId() : null)
                .clinicName(clinic != null ? clinic.getName() : null)
                .consultationPrice(profile.getConsultationPrice())
                .acceptedPaymentMethods(profile.getAcceptedPaymentMethods())
                .paymentTiming(profile.getPaymentTiming())
                .instagramUrl(profile.getInstagramUrl())
                .linkedinUrl(profile.getLinkedinUrl())
                .websiteUrl(profile.getWebsiteUrl())
                .facebookUrl(profile.getFacebookUrl())
                .bio(profile.getBio())
                .councilType(profile.getCouncilType() != null ? profile.getCouncilType().name() : null)
                .councilState(profile.getCouncilState())
                .zipCode(profile.getZipCode())
                .neighborhood(profile.getNeighborhood())
                .streetNumber(profile.getStreetNumber())
                .complement(profile.getComplement())
                .education(profile.getEducation().stream()
                        .map(e -> new ProfessionalEducationDTO(e.getId(), e.getDegree(), e.getInstitution(), e.getFieldOfStudy(), e.getGraduationYear()))
                        .collect(Collectors.toList()))
                .experience(profile.getExperience().stream()
                        .map(e -> new ProfessionalExperienceDTO(e.getId(), e.getPosition(), e.getInstitution(), e.getStartYear(), e.getEndYear(), e.getDescription()))
                        .collect(Collectors.toList()))
                .certificates(profile.getCertificates().stream()
                        .map(c -> new ProfessionalCertificateDTO(c.getId(), c.getTitle(), c.getIssuingOrganization(), c.getIssueYear(), c.getCertificateUrl()))
                        .collect(Collectors.toList()))
                .build();
    }
}
