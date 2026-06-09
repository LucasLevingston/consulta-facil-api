package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Optional<Subscription> findByUserId(String userId);
    Optional<Subscription> findByMpPreferenceId(String preferenceId);
    Optional<Subscription> findByMpPaymentId(String paymentId);
    Optional<Subscription> findByMpPreapprovalId(String mpPreapprovalId);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.expiresAt < :now")
    List<Subscription> findActiveExpiredBefore(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.expiresAt BETWEEN :from AND :to")
    List<Subscription> findActiveExpiringBetween(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
}
