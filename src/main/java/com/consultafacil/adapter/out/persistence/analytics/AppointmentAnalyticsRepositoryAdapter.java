package com.consultafacil.adapter.out.persistence.analytics;

import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.analytics.AppointmentAnalyticsPort;
import com.consultafacil.domain.repository.analytics.AppointmentAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AppointmentAnalyticsRepositoryAdapter implements AppointmentAnalyticsPort {

    private final AppointmentAnalyticsRepository appointmentAnalyticsRepository;

    @Override
    public long count() {
        return appointmentAnalyticsRepository.count();
    }

    @Override
    public long countByStatus(AppointmentStatus status) {
        return appointmentAnalyticsRepository.countByStatus(status);
    }

    @Override
    public List<Object[]> countByMonth(LocalDateTime since) {
        return appointmentAnalyticsRepository.countByMonth(since);
    }

    @Override
    public List<Object[]> groupByStatus() {
        return appointmentAnalyticsRepository.groupByStatus();
    }

    @Override
    public List<Object[]> groupByModality() {
        return appointmentAnalyticsRepository.groupByModality();
    }
}
