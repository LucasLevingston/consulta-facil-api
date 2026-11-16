package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.RatingStatsDTO;
import com.consultafacil.domain.entity.Appointment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class ProfessionalRatingStatsCalculatorTest {

    private final ProfessionalRatingStatsCalculator calculator = new ProfessionalRatingStatsCalculator();

    private Appointment rated(Integer stars) {
        return Appointment.builder().rating(stars).build();
    }

    @Test
    void averagesRatingsRoundedToOneDecimalAndBuildsDistribution() {
        RatingStatsDTO stats = calculator.calculate(List.of(rated(5), rated(5), rated(4), rated(3), rated(null)));

        assertThat(stats.total()).isEqualTo(4);
        assertThat(stats.average()).isEqualTo(4.3);
        assertThat(stats.distribution()).containsExactly(
                entry(1, 0L), entry(2, 0L), entry(3, 1L), entry(4, 1L), entry(5, 2L));
    }

    @Test
    void ignoresOutOfRangeValues() {
        RatingStatsDTO stats = calculator.calculate(List.of(rated(0), rated(6), rated(2)));

        assertThat(stats.total()).isEqualTo(1);
        assertThat(stats.average()).isEqualTo(2.0);
    }

    @Test
    void noRatingsGivesNullAverageAndZeroDistribution() {
        RatingStatsDTO stats = calculator.calculate(List.of(rated(null)));

        assertThat(stats.average()).isNull();
        assertThat(stats.total()).isZero();
        assertThat(stats.distribution()).hasSize(5).allSatisfy((k, v) -> assertThat(v).isZero());
    }
}
