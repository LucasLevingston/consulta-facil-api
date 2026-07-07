package com.consultafacil.core.seeder.appointment;

import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Component
public class AppointmentFakeDataHelper {

    private final Faker faker = new Faker(new Locale("pt-BR"));

    public LocalDateTime resolveScheduledAt(AppointmentStatus status) {
        int hour = faker.random().nextInt(8, 18);
        int minute = List.of(0, 30).get(faker.random().nextInt(2));
        return switch (status) {
            case COMPLETED -> LocalDateTime.now()
                    .minusDays(faker.random().nextInt(1, 180))
                    .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
            case CONFIRMED -> LocalDateTime.now()
                    .plusDays(faker.random().nextInt(1, 15))
                    .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
            case CANCELED -> LocalDateTime.now()
                    .minusDays(faker.random().nextInt(1, 60))
                    .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
            default -> LocalDateTime.now()
                    .plusDays(faker.random().nextInt(5, 90))
                    .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        };
    }

    public void enrichCompletedAppointment(Appointment appointment) {
        appointment.setNotes(faker.lorem().paragraph());
        if (faker.bool().bool()) {
            appointment.setRating(3 + faker.random().nextInt(3));
            appointment.setRatingComment(faker.lorem().sentence());
        }
        int roll = faker.random().nextInt(100);
        if (roll < 70) {
            appointment.setPaymentStatus(AppointmentPaymentStatus.PAID);
            appointment.setPaymentAmount(BigDecimal.valueOf(150 + faker.random().nextInt(351)));
        } else if (roll < 90) {
            appointment.setPaymentStatus(AppointmentPaymentStatus.PENDING_PAYMENT);
            appointment.setPaymentAmount(BigDecimal.valueOf(150 + faker.random().nextInt(351)));
        }
    }
}
