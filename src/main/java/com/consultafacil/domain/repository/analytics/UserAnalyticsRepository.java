package com.consultafacil.domain.repository.analytics;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserAnalyticsRepository extends JpaRepository<User, String> {

    long countByRole(UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countNewSince(@Param("since") LocalDateTime since);

    @Query("SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) FROM User u " +
           "WHERE u.createdAt >= :since " +
           "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) " +
           "ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)")
    List<Object[]> growthByMonth(@Param("since") LocalDateTime since);

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> groupByRole();
}
