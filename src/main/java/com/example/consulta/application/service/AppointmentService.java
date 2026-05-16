package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.appointment.CancelAppointmentDTO;
import com.example.consulta.api.dto.appointment.CreateAppointmentDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.DoctorProfile;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.DoctorProfileRepository;
import com.example.consulta.domain.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Transactional
    public AppointmentResponseDTO scheduleAppointment(String userId, CreateAppointmentDTO dto) {
        log.info("Scheduling appointment for user: {} with doctor: {}", userId, dto.getDoctorId());

        PatientProfile patient = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        DoctorProfile doctor = doctorProfileRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.getDoctorId()));

        if (appointmentRepository.existsByDoctorIdAndScheduledAt(dto.getDoctorId(), dto.getScheduledAt())) {
            throw new BadRequestException("Doctor already has an appointment scheduled at this time");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledAt(dto.getScheduledAt())
                .reason(dto.getReason())
                .notes(dto.getNotes())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment scheduled successfully: {}", saved.getId());
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(String appointmentId) {
        log.debug("Fetching appointment by ID: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        return toResponseDTO(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getPatientAppointments(String userId, Pageable pageable) {
        log.debug("Fetching appointments for user: {}", userId);
        PatientProfile patient = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));
        return appointmentRepository.findByPatientId(patient.getId(), pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getDoctorAppointments(String doctorId, Pageable pageable) {
        log.debug("Fetching appointments for doctor: {}", doctorId);
        doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        return appointmentRepository.findByDoctorId(doctorId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    public AppointmentResponseDTO confirmAppointment(String appointmentId) {
        log.info("Confirming appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("Only pending appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment confirmed: {}", appointmentId);
        return toResponseDTO(updated);
    }

    @Transactional
    public AppointmentResponseDTO cancelAppointment(String appointmentId, CancelAppointmentDTO dto) {
        log.info("Cancelling appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
            appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new BadRequestException("Cannot cancel an appointment with status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setCancellationReason(dto.getCancellationReason());
        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment cancelled: {}", appointmentId);
        return toResponseDTO(updated);
    }

    @Transactional
    public AppointmentResponseDTO completeAppointment(String appointmentId) {
        log.info("Completing appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed appointments can be marked as completed");
        }

        if (appointment.getScheduledAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Appointment has not occurred yet");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment completed: {}", appointmentId);
        return toResponseDTO(updated);
    }

    @Transactional
    public void deleteAppointment(String appointmentId) {
        log.info("Deleting appointment: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        appointmentRepository.delete(appointment);
        log.info("Appointment deleted: {}", appointmentId);
    }

    private AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient().getUser().getName())
                .patientId(appointment.getPatient().getId())
                .doctorName(appointment.getDoctor().getUser().getName())
                .doctorId(appointment.getDoctor().getId())
                .specialty(appointment.getDoctor().getSpecialty())
                .scheduledAt(appointment.getScheduledAt())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .status(appointment.getStatus())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
