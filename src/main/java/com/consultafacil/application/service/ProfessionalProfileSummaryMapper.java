package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfessionalProfileSummaryMapper {

    private final ProfessionalRatingCalculator ratingCalculator;
    private final ProfessionalConsultationCountCalculator consultationCountCalculator;

    public ProfessionalResponseDTO toListSummaryDTO(ProfessionalProfile profile) {
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
                .consultationCount(consultationCountCalculator.computeConsultationCount(profile))
                .status(profile.getStatus())
                .city(profile.getCity())
                .state(profile.getState())
                .address(profile.getAddress())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .clinicId(null)
                .clinicName(null)
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
                .build();
    }
}
