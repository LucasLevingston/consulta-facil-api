package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.ProfessionalAnalyticsDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProfessionalAnalyticsServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;

    GetProfessionalAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new GetProfessionalAnalyticsService(professionalProfileRepository, appointmentRepository,
                new ProfessionalRevenueCalculator(), new ProfessionalAppointmentStatsCalculator(),
                new ProfessionalRatingStatsCalculator(), new ProfessionalTopServicesCalculator());
    }

    @ParameterizedTest
    @CsvSource({"30d, 30", "90d, 90", "12m, 365"})
    void execute_loadsTheProfessionalsAppointmentsForThePeriod(String code, long approxDays) {
        when(professionalProfileRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(ProfessionalProfile.builder().id("prof-1").build()));
        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetween(eq("prof-1"), any(), any()))
                .thenReturn(List.of());

        ProfessionalAnalyticsDTO result = service.execute("user-1", code);

        ArgumentCaptor<LocalDateTime> from = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> to = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(appointmentRepository).findByProfessionalIdAndScheduledAtBetween(eq("prof-1"), from.capture(), to.capture());
        assertThat(to.getValue()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.MINUTES));
        assertThat(ChronoUnit.DAYS.between(from.getValue(), to.getValue())).isBetween(approxDays - 1, approxDays + 1);
        assertThat(result.period()).isEqualTo(code);
        assertThat(result.from()).isEqualTo(from.getValue());
        assertThat(result.to()).isEqualTo(to.getValue());
    }

    @Test
    void execute_combinesEveryCalculatorIntoOneResponse() {
        when(professionalProfileRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(ProfessionalProfile.builder().id("prof-1").build()));
        LocalDateTime recent = LocalDateTime.now().minusDays(2);
        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetween(eq("prof-1"), any(), any()))
                .thenReturn(List.of(
                        Appointment.builder().scheduledAt(recent).status(AppointmentStatus.COMPLETED)
                                .paymentStatus(AppointmentPaymentStatus.PAID).paymentAmount(new BigDecimal("250.00"))
                                .rating(5).build(),
                        Appointment.builder().scheduledAt(recent).status(AppointmentStatus.CANCELED).build()));

        ProfessionalAnalyticsDTO result = service.execute("user-1", "30d");

        assertThat(result.revenue().total()).isEqualByComparingTo("250.00");
        assertThat(result.appointments().total()).isEqualTo(2);
        assertThat(result.appointments().cancellationRate()).isEqualByComparingTo("0.5");
        assertThat(result.ratings().average()).isEqualTo(5.0);
        assertThat(result.topServices()).isEmpty();
    }

    @Test
    void execute_acceptsUppercasePeriodCodes() {
        when(professionalProfileRepository.findByUserId("user-1"))
                .thenReturn(Optional.of(ProfessionalProfile.builder().id("prof-1").build()));
        when(appointmentRepository.findByProfessionalIdAndScheduledAtBetween(eq("prof-1"), any(), any()))
                .thenReturn(List.of());

        assertThat(service.execute("user-1", "12M").period()).isEqualTo("12m");
    }

    @Test
    void execute_rejectsUnknownPeriodBeforeTouchingTheDatabase() {
        assertThatThrownBy(() -> service.execute("user-1", "7d"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("30d, 90d ou 12m");
        verifyNoInteractions(professionalProfileRepository, appointmentRepository);
    }

    @Test
    void execute_requiresAProfessionalProfile() {
        when(professionalProfileRepository.findByUserId("patient-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("patient-1", "30d"))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(appointmentRepository);
    }
}
