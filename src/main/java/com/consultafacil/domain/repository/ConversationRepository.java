package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @EntityGraph(attributePaths = {"patient", "professional"})
    Optional<Conversation> findByPatientIdAndProfessionalId(String patientId, String professionalId);

    @EntityGraph(attributePaths = {"patient", "professional"})
    @Query("SELECT c FROM Conversation c WHERE c.patient.id = :userId OR c.professional.id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByUserId(@Param("userId") String userId);

    @EntityGraph(attributePaths = {"patient", "professional"})
    Optional<Conversation> findById(String id);
}
