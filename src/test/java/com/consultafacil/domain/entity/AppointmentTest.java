package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.exception.InvalidStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class AppointmentTest {

    Appointment appt;
    PatientProfile patient;
    ProfessionalProfile professional;

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@e.com").name("João").password("x").role(UserRole.PATIENT).build();
        patient = new PatientProfile(); patient.setId("pp-1"); patient.setUser(pUser);

        User dUser = User.builder().id("u-2").email("d@e.com").name("Dra.Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile(); professional.setId("pr-1"); professional.setUser(dUser);
        professional.setSpecialty("Cardio");

        appt = Appointment.schedule(patient, professional, LocalDateTime.now().plusDays(1),
                "Consulta", null, AppointmentModality.IN_PERSON, null, null, null);
    }

    // confirm
    @Test void confirm_pending_becomesConfirmed() {
        appt.confirm();
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }
    @Test void confirm_notPending_throws() {
        appt.setStatus(AppointmentStatus.CONFIRMED);
        assertThatThrownBy(appt::confirm).isInstanceOf(InvalidStateException.class);
    }

    // cancel
    @Test void cancel_confirmed_becomesCanceled() {
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.cancel("Motivo");
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(appt.getCancellationReason()).isEqualTo("Motivo");
    }
    @Test void cancel_completed_throws() {
        appt.setStatus(AppointmentStatus.COMPLETED);
        assertThatThrownBy(() -> appt.cancel("r")).isInstanceOf(InvalidStateException.class);
    }
    @Test void cancel_alreadyCanceled_throws() {
        appt.setStatus(AppointmentStatus.CANCELED);
        assertThatThrownBy(() -> appt.cancel("r")).isInstanceOf(InvalidStateException.class);
    }

    // complete
    @Test void complete_confirmed_becomesCompleted() {
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.complete();
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }
    @Test void complete_inProgress_becomesCompleted() {
        appt.setStatus(AppointmentStatus.IN_PROGRESS);
        appt.complete();
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }
    @Test void complete_pending_throws() {
        assertThatThrownBy(appt::complete).isInstanceOf(InvalidStateException.class);
    }

    // checkIn
    @Test void checkIn_confirmed_becomesCheckedIn() {
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.checkIn();
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN);
        assertThat(appt.getCheckedInAt()).isNotNull();
    }
    @Test void checkIn_completed_throws() {
        appt.setStatus(AppointmentStatus.COMPLETED);
        assertThatThrownBy(appt::checkIn).isInstanceOf(InvalidStateException.class);
    }

    // callNext
    @Test void callNext_checkedIn_becomesInProgress() {
        appt.setStatus(AppointmentStatus.CHECKED_IN);
        appt.callNext();
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.IN_PROGRESS);
        assertThat(appt.getCalledAt()).isNotNull();
    }
    @Test void callNext_notCheckedIn_throws() {
        assertThatThrownBy(appt::callNext).isInstanceOf(InvalidStateException.class);
    }

    // reschedule
    @Test void reschedule_pending_updatesDate() {
        LocalDateTime newDate = LocalDateTime.now().plusDays(5);
        appt.reschedule(newDate, "Novo motivo");
        assertThat(appt.getScheduledAt()).isEqualTo(newDate);
        assertThat(appt.getReason()).isEqualTo("Novo motivo");
        assertThat(appt.getPreviousScheduledAt()).isNotNull();
    }
    @Test void reschedule_confirmed_works() {
        appt.setStatus(AppointmentStatus.CONFIRMED);
        appt.reschedule(LocalDateTime.now().plusDays(7), null);
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }
    @Test void reschedule_completed_throws() {
        appt.setStatus(AppointmentStatus.COMPLETED);
        assertThatThrownBy(() -> appt.reschedule(LocalDateTime.now().plusDays(1), null))
                .isInstanceOf(InvalidStateException.class);
    }

    // rate
    @Test void rate_completed_setsRating() {
        appt.setStatus(AppointmentStatus.COMPLETED);
        appt.rate(5, "Excelente!");
        assertThat(appt.getRating()).isEqualTo(5);
        assertThat(appt.getRatingComment()).isEqualTo("Excelente!");
    }
    @Test void rate_notCompleted_throws() {
        assertThatThrownBy(() -> appt.rate(4, null)).isInstanceOf(InvalidStateException.class);
    }
    @Test void rate_alreadyRated_throws() {
        appt.setStatus(AppointmentStatus.COMPLETED);
        appt.rate(4, null);
        assertThatThrownBy(() -> appt.rate(5, null)).isInstanceOf(InvalidStateException.class);
    }
}
