package com.example.consulta.application.service;

import com.example.consulta.api.dto.doctor.CreateDoctorDTO;
import com.example.consulta.api.dto.doctor.DoctorResponseDTO;
import com.example.consulta.core.exception.DuplicateResourceException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.DoctorProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.DoctorProfileRepository;
import com.example.consulta.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public DoctorResponseDTO createDoctorProfile(String userId, CreateDoctorDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (doctorProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("Doctor", "license number", dto.getLicenseNumber());
        }

        DoctorProfile doctorProfile = DoctorProfile.builder()
                .user(user)
                .specialty(dto.getSpecialty())
                .licenseNumber(dto.getLicenseNumber())
                .build();

        DoctorProfile saved = doctorProfileRepository.save(doctorProfile);
        user.setRole(UserRole.DOCTOR);
        userRepository.save(user);

        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(String doctorId) {
        log.debug("Fetching doctor by ID: {}", doctorId);
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        return toResponseDTO(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorByUserId(String userId) {
        log.debug("Fetching doctor by user ID: {}", userId);
        DoctorProfile doctor = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + userId));
        return toResponseDTO(doctor);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> searchDoctorsBySpecialty(String specialty, Pageable pageable) {
        log.debug("Searching doctors by specialty: {}", specialty);
        return doctorProfileRepository.findBySpecialtyContainingIgnoreCase(specialty, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> getAllDoctors(Pageable pageable) {
        log.debug("Fetching all doctors");
        return doctorProfileRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(String doctorId, CreateDoctorDTO dto) {

        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        if (!doctor.getLicenseNumber().equals(dto.getLicenseNumber()) &&
                doctorProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new DuplicateResourceException("Doctor", "license number", dto.getLicenseNumber());
        }

        doctor.setSpecialty(dto.getSpecialty());
        doctor.setLicenseNumber(dto.getLicenseNumber());

        DoctorProfile updated = doctorProfileRepository.save(doctor);
        return toResponseDTO(updated);
    }

    @Transactional
    public void deleteDoctor(String doctorId) {
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        User user = doctor.getUser();
        user.setRole(UserRole.PATIENT);
        userRepository.save(user);

        doctorProfileRepository.delete(doctor);
    }

    private DoctorResponseDTO toResponseDTO(DoctorProfile doctor) {
        int consultationCount = (int) doctor.getAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .name(doctor.getUser().getName())
                .email(doctor.getUser().getEmail())
                .specialty(doctor.getSpecialty())
                .licenseNumber(doctor.getLicenseNumber())
                .phone(doctor.getUser().getPhone())
                .imageUrl(doctor.getUser().getImageUrl())
                .rating(doctor.getRating())
                .consultationCount(consultationCount)
                .build();
    }
}
