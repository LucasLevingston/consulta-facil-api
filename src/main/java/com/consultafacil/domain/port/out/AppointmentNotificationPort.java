package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.Appointment;

public interface AppointmentNotificationPort {

    void notifyScheduled(Appointment appointment);

    void notifyConfirmed(Appointment appointment);

    void notifyCanceled(Appointment appointment);
}
