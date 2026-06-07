package com.example.consulta.application.service;

import com.example.consulta.api.dto.professional.CreateProfessionalDTO;
import com.example.consulta.api.dto.professional.ProfessionalResponseDTO;
import com.example.consulta.core.exception.DuplicateResourceException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
import com.example.consulta.application.port.in.ProfessionalProfileUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessionalService implements ProfessionalProfileUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final UserRepositoryPort userRepository;

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

        return toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Cacheable(value = "professional-profile", key = "#professionalId")
    @Transactional(readOnly = true)
    public ProfessionalResponseDTO getProfessionalById(String professionalId) {
        log.debug("Fetching professional by ID: {}", professionalId);
        return toResponseDTO(professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId)));
    }

    @Transactional(readOnly = true)
    public ProfessionalResponseDTO getProfessionalByUserId(String userId) {
        log.debug("Fetching professional by user ID: {}", userId);
        return toResponseDTO(professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professional profile not found for user: " + userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfessionalResponseDTO> searchBySpecialty(String specialty, Pageable pageable) {
        return professionalProfileRepository
                .findBySpecialtyContainingIgnoreCaseAndStatus(specialty, ProfessionalProfileStatus.ACTIVE, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProfessionalResponseDTO> getAllProfessionals(String profession, String specialty, String name, Pageable pageable) {
        String profParam = (profession != null && !profession.isBlank()) ? profession : "";
        String specParam = (specialty != null && !specialty.isBlank()) ? specialty : "";
        String nameParam = (name != null && !name.isBlank()) ? name : "";
        return professionalProfileRepository.findActiveWithFilters(profParam, specParam, nameParam, pageable)
                .map(this::toListSummaryDTO);
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponseDTO> getProfessionalsNearby(double lat, double lng, double radiusKm, String specialty, String profession) {
        String specParam = (specialty != null && !specialty.isBlank()) ? specialty : "";
        String profParam = (profession != null && !profession.isBlank()) ? profession : "";
        return professionalProfileRepository.findNearby(lat, lng, radiusKm, specParam, profParam)
                .stream().map(this::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProfessionalResponseDTO> getPendingApplications(Pageable pageable) {
        return professionalProfileRepository.findByStatus(ProfessionalProfileStatus.PENDING_REVIEW, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalResponseDTO getApplicationStatus(String userId) {
        return toResponseDTO(professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professional application not found for user: " + userId)));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO approveApplication(String professionalId) {
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));

        profile.approve(); // throws InvalidStateException if not PENDING_REVIEW

        User user = profile.getUser();
        user.promote(UserRole.PROFESSIONAL);
        userRepository.save(user);

        return toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public ProfessionalResponseDTO rejectApplication(String professionalId) {
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));

        profile.reject(); // throws InvalidStateException if not PENDING_REVIEW

        return toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Caching(evict = {
        @CacheEvict(value = "professional-profile", key = "#professionalId"),
        @CacheEvict(value = "professional-services", allEntries = true)
    })
    @Override
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

        return toResponseDTO(professionalProfileRepository.save(profile));
    }

    @Caching(evict = {
        @CacheEvict(value = "professional-profile", key = "#professionalId"),
        @CacheEvict(value = "professional-services", allEntries = true)
    })
    @Override
    @Transactional
    public void deleteProfessional(String professionalId) {
        ProfessionalProfile profile = professionalProfileRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalId));

        profile.getUser().promote(UserRole.PATIENT);
        userRepository.save(profile.getUser());

        professionalProfileRepository.delete(profile);
    }

    // --- bridges ---
    @Override public ProfessionalResponseDTO createProfile(String userId, CreateProfessionalDTO dto) { return createProfessionalProfile(userId, dto); }
    @Override public ProfessionalResponseDTO getById(String id) { return getProfessionalById(id); }
    @Override public ProfessionalResponseDTO getByUserId(String userId) { return getProfessionalByUserId(userId); }
    @Override public Page<ProfessionalResponseDTO> getAll(String profession, String specialty, String name, Pageable pageable) { return getAllProfessionals(profession, specialty, name, pageable); }
    @Override public List<ProfessionalResponseDTO> getNearby(double lat, double lng, double radiusKm, String specialty, String profession) { return getProfessionalsNearby(lat, lng, radiusKm, specialty, profession); }

    // --- mappers ---

    private ProfessionalResponseDTO toListSummaryDTO(ProfessionalProfile profile) {
        Double rating = computeRating(profile);
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
                .consultationCount(computeConsultationCount(profile))
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
                .build();
    }

    public ProfessionalResponseDTO toResponseDTO(ProfessionalProfile profile) {
        Double rating = computeRating(profile);

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
                .consultationCount(computeConsultationCount(profile))
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

    private Double computeRating(ProfessionalProfile profile) {
        return profile.getAppointments().stream()
                .filter(a -> a.getRating() != null)
                .mapToInt(a -> a.getRating())
                .average()
                .stream()
                .boxed()
                .map(avg -> Math.round(avg * 10.0) / 10.0)
                .findFirst()
                .orElse(null);
    }

    private int computeConsultationCount(ProfessionalProfile profile) {
        return (int) profile.getAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
    }
}
