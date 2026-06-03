package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.Appointment;

public interface AppointmentNotificationPort {

    void notifyScheduled(Appointment appointment);

    void notifyConfirmed(Appointment appointment);

    void notifyCanceled(Appointment appointment);
}
