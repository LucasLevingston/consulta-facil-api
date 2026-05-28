package com.example.consulta.application.service;

import com.example.consulta.api.dto.professional.CreateProfessionalDTO;
import com.example.consulta.api.dto.professional.ProfessionalResponseDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.DuplicateResourceException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import com.example.consulta.domain.enums.UserRole;
import java.util.List;
import java.util.OptionalDouble;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final ProfessionalProfileRepository professionalProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProfessionalResponseDTO createProfessionalProfile(String userId, CreateProfessionalDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (professionalProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("Professional", "license number", dto.getLicenseNumber());
        }

        ProfessionalProfile profile = ProfessionalProfile.builder()
                .user(user)
                .profession(dto.getProfession())
                .specialty(dto.getSpecialty())
                .licenseNumber(dto.getLicenseNumber())
                .status(ProfessionalProfileStatus.PENDING_REVIEW)
                .build();

        ProfessionalProfile saved = professionalProfileRepository.save(profile);
        return toResponseDTO(saved);
    }

    @Cacheable(value = "professional-profile", key = "#professionalId")
    @Transactional(readOnly = true)
    public ProfessionalResponseDTO getProfessionalById(String professionalId) {
        log.debug("Fetching professional by ID: {}", professionalId);
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));
        return toResponseDTO(profile);
    }

    @Transactional(readOnly = true)
    public ProfessionalResponseDTO getProfessionalByUserId(String userId) {
        log.debug("Fetching professional by user ID: {}", userId);
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional profile not found for user: " + userId));
        return toResponseDTO(profile);
    }

    @Transactional(readOnly = true)
    public Page<ProfessionalResponseDTO> searchBySpecialty(String specialty, Pageable pageable) {
        log.debug("Searching professionals by specialty: {}", specialty);
        return professionalProfileRepository
                .findBySpecialtyContainingIgnoreCaseAndStatus(specialty, ProfessionalProfileStatus.ACTIVE, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProfessionalResponseDTO> getAllProfessionals(String profession, String specialty, String name, Pageable pageable) {
        log.debug("Fetching professionals with filters profession={}, specialty={}, name={}", profession, specialty, name);
        String profParam = (profession != null && !profession.isBlank()) ? profession : "";
        String specParam = (specialty != null && !specialty.isBlank()) ? specialty : "";
        String nameParam = (name != null && !name.isBlank()) ? name : "";
        return professionalProfileRepository.findActiveWithFilters(profParam, specParam, nameParam, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponseDTO> getProfessionalsNearby(double lat, double lng, double radiusKm, String specialty, String profession) {
        log.debug("Fetching professionals near ({}, {}) within {}km", lat, lng, radiusKm);
        String specParam = (specialty != null && !specialty.isBlank()) ? specialty : "";
        String profParam = (profession != null && !profession.isBlank()) ? profession : "";
        return professionalProfileRepository.findNearby(lat, lng, radiusKm, specParam, profParam)
                .stream().map(this::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProfessionalResponseDTO> getPendingApplications(Pageable pageable) {
        log.debug("Fetching pending professional applications");
        return professionalProfileRepository.findByStatus(ProfessionalProfileStatus.PENDING_REVIEW, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProfessionalResponseDTO getApplicationStatus(String userId) {
        log.debug("Fetching professional application status for user: {}", userId);
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional application not found for user: " + userId));
        return toResponseDTO(profile);
    }

    @Transactional
    public ProfessionalResponseDTO approveApplication(String professionalId) {
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));

        if (profile.getStatus() != ProfessionalProfileStatus.PENDING_REVIEW) {
            throw new BadRequestException("Application is not pending review");
        }

        profile.setStatus(ProfessionalProfileStatus.ACTIVE);
        User user = profile.getUser();
        user.setRole(UserRole.PROFESSIONAL);
        userRepository.save(user);
        return toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Transactional
    public ProfessionalResponseDTO rejectApplication(String professionalId) {
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));

        if (profile.getStatus() != ProfessionalProfileStatus.PENDING_REVIEW) {
            throw new BadRequestException("Application is not pending review");
        }

        profile.setStatus(ProfessionalProfileStatus.REJECTED);
        return toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Caching(evict = {
        @CacheEvict(value = "professional-profile", key = "#professionalId"),
        @CacheEvict(value = "professional-services", allEntries = true)
    })
    @Transactional
    public ProfessionalResponseDTO updateProfessional(String professionalId, CreateProfessionalDTO dto) {

        ProfessionalProfile profile = professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));

        if (!profile.getLicenseNumber().equals(dto.getLicenseNumber()) &&
                professionalProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("Professional", "license number", dto.getLicenseNumber());
        }

        profile.setProfession(dto.getProfession());
        profile.setSpecialty(dto.getSpecialty());
        profile.setLicenseNumber(dto.getLicenseNumber());

        ProfessionalProfile updated = professionalProfileRepository.save(profile);
        return toResponseDTO(updated);
    }

    @Caching(evict = {
        @CacheEvict(value = "professional-profile", key = "#professionalId"),
        @CacheEvict(value = "professional-services", allEntries = true)
    })
    @Transactional
    public void deleteProfessional(String professionalId) {
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));

        User user = profile.getUser();
        user.setRole(UserRole.PATIENT);
        userRepository.save(user);

        professionalProfileRepository.delete(profile);
    }

    public ProfessionalResponseDTO toResponseDTO(ProfessionalProfile profile) {
        int consultationCount = (int) profile.getAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
        OptionalDouble avg = profile.getAppointments().stream()
                .filter(a -> a.getRating() != null)
                .mapToInt(a -> a.getRating())
                .average();
        Double rating = avg.isPresent()
                ? Math.round(avg.getAsDouble() * 10.0) / 10.0
                : null;

        Clinic clinic = profile.getClinicMemberships().stream()
                .findFirst()
                .map(m -> m.getClinic())
                .orElse(null);

        return ProfessionalResponseDTO.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .profession(profile.getProfession())
                .specialty(profile.getSpecialty())
                .licenseNumber(profile.getLicenseNumber())
                .phone(profile.getUser().getPhone())
                .imageUrl(profile.getUser().getImageUrl())
                .rating(rating)
                .consultationCount(consultationCount)
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
                .build();
    }
}
