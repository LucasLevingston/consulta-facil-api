package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalRatingDTO;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetProfessionalRatingsServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;

    GetProfessionalRatingsService service;

    @BeforeEach
    void setUp() {
        service = new GetProfessionalRatingsService(appointmentRepository, new RatingDistributionBuilder());
    }

    @Test
    void execute_withRatings_returnsCorrectAverage() {
        when(appointmentRepository.findAverageRatingByProfessionalId("prof-1")).thenReturn(4.666);
        when(appointmentRepository.findRatingDistributionByProfessionalId("prof-1"))
                .thenReturn(List.of(
                        new Object[]{5, 3L},
                        new Object[]{4, 1L},
                        new Object[]{3, 1L}
                ));

        ProfessionalRatingDTO result = service.execute("prof-1");

        assertThat(result.averageRating()).isEqualTo(4.7);
        assertThat(result.totalRatings()).isEqualTo(5L);
    }

    @Test
    void execute_withRatings_returnsDistributionMap() {
        when(appointmentRepository.findAverageRatingByProfessionalId("prof-1")).thenReturn(5.0);
        when(appointmentRepository.findRatingDistributionByProfessionalId("prof-1"))
                .thenReturn(List.<Object[]>of(new Object[]{5, 2L}));

        ProfessionalRatingDTO result = service.execute("prof-1");

        assertThat(result.distribution()).containsEntry(5, 2L);
        assertThat(result.distribution()).containsEntry(1, 0L);
    }

    @Test
    void execute_noRatings_returnsNullAverageAndZeroTotal() {
        when(appointmentRepository.findAverageRatingByProfessionalId("prof-empty")).thenReturn(null);
        when(appointmentRepository.findRatingDistributionByProfessionalId("prof-empty"))
                .thenReturn(List.of());

        ProfessionalRatingDTO result = service.execute("prof-empty");

        assertThat(result.averageRating()).isNull();
        assertThat(result.totalRatings()).isZero();
    }
}
