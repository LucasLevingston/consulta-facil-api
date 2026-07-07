package com.consultafacil.adapter.out.persistence.appointment;

import com.consultafacil.domain.PatientSummary;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.repository.appointment.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepositoryPort {

    private final AppointmentRepository appointmentRepository;

    @Override
    public Page<Appointment> findAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @Override
    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    @Override
    public Optional<Appointment> findById(String id) {
        return appointmentRepository.findById(id);
    }

    @Override
    public Page<Appointment> findByPatientId(String patientId, Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, pageable);
    }

    @Override
    public Page<Appointment> findByProfessionalId(String professionalId, Pageable pageable) {
        return appointmentRepository.findByProfessionalId(professionalId, pageable);
    }

    @Override
    public Page<Appointment> findByProfessionalIdAndSource(String professionalId, AppointmentSource source, Pageable pageable) {
        return appointmentRepository.findByProfessionalIdAndSource(professionalId, source, pageable);
    }

    @Override
    public List<Appointment> findByPatientIdAndStatus(String patientId, AppointmentStatus status) {
        return appointmentRepository.findByPatientIdAndStatus(patientId, status);
    }

    @Override
    public List<Appointment> findByProfessionalIdAndStatusAndScheduledAtBetween(
            String professionalId, AppointmentStatus status, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByProfessionalIdAndStatusAndScheduledAtBetween(
                professionalId, status, start, end);
    }

    @Override
    public boolean existsByProfessionalIdAndScheduledAt(String professionalId, LocalDateTime scheduledAt) {
        return appointmentRepository.existsByProfessionalIdAndScheduledAt(professionalId, scheduledAt);
    }

    @Override
    public List<Appointment> findByProfessionalIdAndStatusInAndScheduledAtBetweenOrderByCheckedInAt(
            String professionalId, List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByProfessionalIdAndStatusInAndScheduledAtBetweenOrderByCheckedInAt(
                professionalId, statuses, start, end);
    }

    @Override
    public List<Appointment> findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(
            List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(
                statuses, start, end);
    }

    @Override
    public List<Appointment> findClinicQueueAppointments(
            String clinicId, List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findClinicQueueAppointments(clinicId, statuses, start, end);
    }

    @Override
    public Page<PatientSummary> findProfessionalPatientsByName(
            String professionalId, String search, Pageable pageable) {
        return appointmentRepository.findProfessionalPatientsByName(professionalId, search, pageable);
    }

    @Override
    public Page<PatientSummary> findProfessionalPatientsByRecent(
            String professionalId, String search, Pageable pageable) {
        return appointmentRepository.findProfessionalPatientsByRecent(professionalId, search, pageable);
    }

    @Override
    public void delete(Appointment appointment) {
        appointmentRepository.delete(appointment);
    }

    @Override
    public List<Object[]> findRatingDistributionByProfessionalId(String professionalId) {
        return appointmentRepository.findRatingDistributionByProfessionalId(professionalId);
    }

    @Override
    public Double findAverageRatingByProfessionalId(String professionalId) {
        return appointmentRepository.findAverageRatingByProfessionalId(professionalId);
    }
}
