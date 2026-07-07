package com.consultafacil.domain.repository;

import com.consultafacil.domain.PatientSummary;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    // Eager-load patient.user + professional.user + service to eliminate N+1 on list endpoints
    @EntityGraph(attributePaths = {"patient.user", "professional.user", "service"})
    @Override
    Page<Appointment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"patient.user", "professional.user", "service"})
    Page<Appointment> findByPatientId(String patientId, Pageable pageable);

    @EntityGraph(attributePaths = {"patient.user", "professional.user", "service"})
    Page<Appointment> findByProfessionalId(String professionalId, Pageable pageable);

    @EntityGraph(attributePaths = {"patient.user", "professional.user", "service"})
    Page<Appointment> findByProfessionalIdAndSource(String professionalId, AppointmentSource source, Pageable pageable);

    @EntityGraph(attributePaths = {"patient.user", "professional.user", "service"})
    Optional<Appointment> findById(String id);

    List<Appointment> findByPatientIdAndStatus(String patientId, AppointmentStatus status);
    List<Appointment> findByProfessionalIdAndStatusAndScheduledAtBetween(
            String professionalId, AppointmentStatus status, LocalDateTime start, LocalDateTime end);
    boolean existsByProfessionalIdAndScheduledAt(String professionalId, LocalDateTime scheduledAt);

    @EntityGraph(attributePaths = {"patient.user", "professional.user"})
    List<Appointment> findByProfessionalIdAndStatusInAndScheduledAtBetweenOrderByCheckedInAt(
            String professionalId, List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"patient.user", "professional.user"})
    List<Appointment> findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(
            List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT a FROM Appointment a
            JOIN FETCH a.professional p
            JOIN FETCH p.user
            JOIN p.clinicMemberships m
            WHERE m.clinic.id = :clinicId
            AND a.status IN :statuses
            AND a.scheduledAt BETWEEN :start AND :end
            ORDER BY a.checkedInAt ASC NULLS LAST
            """)
    List<Appointment> findClinicQueueAppointments(
            @Param("clinicId") String clinicId,
            @Param("statuses") List<AppointmentStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT new com.consultafacil.domain.PatientSummary(
                u.id, u.name, MAX(a.scheduledAt), COUNT(a.id))
            FROM Appointment a
            JOIN a.patient p
            JOIN p.user u
            WHERE a.professional.id = :professionalId
            AND (:search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
            GROUP BY u.id, u.name
            ORDER BY u.name ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id)
            FROM Appointment a JOIN a.patient p JOIN p.user u
            WHERE a.professional.id = :professionalId
            AND (:search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<PatientSummary> findProfessionalPatientsByName(
            @Param("professionalId") String professionalId,
            @Param("search") String search,
            Pageable pageable);

    @Query(value = """
            SELECT new com.consultafacil.domain.PatientSummary(
                u.id, u.name, MAX(a.scheduledAt), COUNT(a.id))
            FROM Appointment a
            JOIN a.patient p
            JOIN p.user u
            WHERE a.professional.id = :professionalId
            AND (:search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
            GROUP BY u.id, u.name
            ORDER BY MAX(a.scheduledAt) DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id)
            FROM Appointment a JOIN a.patient p JOIN p.user u
            WHERE a.professional.id = :professionalId
            AND (:search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<PatientSummary> findProfessionalPatientsByRecent(
            @Param("professionalId") String professionalId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT a.rating, COUNT(a) FROM Appointment a " +
           "WHERE a.professional.id = :professionalId AND a.rating IS NOT NULL " +
           "GROUP BY a.rating ORDER BY a.rating")
    List<Object[]> findRatingDistributionByProfessionalId(@Param("professionalId") String professionalId);

    @Query("SELECT AVG(a.rating) FROM Appointment a " +
           "WHERE a.professional.id = :professionalId AND a.rating IS NOT NULL")
    Double findAverageRatingByProfessionalId(@Param("professionalId") String professionalId);
}
