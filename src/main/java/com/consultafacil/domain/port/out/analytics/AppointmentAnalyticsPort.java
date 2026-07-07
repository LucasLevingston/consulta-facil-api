package com.consultafacil.domain.port.out.analytics;

import com.consultafacil.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentAnalyticsPort {

    long count();

    long countByStatus(AppointmentStatus status);

    List<Object[]> countByMonth(LocalDateTime since);

    List<Object[]> groupByStatus();

    List<Object[]> groupByModality();
}
