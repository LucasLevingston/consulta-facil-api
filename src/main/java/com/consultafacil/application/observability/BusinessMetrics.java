package com.consultafacil.application.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    private Counter appointmentCreated;
    private Counter appointmentCanceled;
    private Counter appointmentRescheduled;
    private Counter procedureRequestCreated;
    private Counter procedureRequestScheduled;
    private Counter procedureRequestCanceled;
    private Counter paymentSucceeded;
    private Counter paymentFailed;

    @PostConstruct
    void init() {
        appointmentCreated = Counter.builder("business.appointments.created")
                .description("Total appointments created")
                .register(meterRegistry);

        appointmentCanceled = Counter.builder("business.appointments.canceled")
                .description("Total appointments canceled")
                .register(meterRegistry);

        appointmentRescheduled = Counter.builder("business.appointments.rescheduled")
                .description("Total appointments rescheduled")
                .register(meterRegistry);

        procedureRequestCreated = Counter.builder("business.procedure_requests.created")
                .description("Total procedure requests created by professionals")
                .register(meterRegistry);

        procedureRequestScheduled = Counter.builder("business.procedure_requests.scheduled")
                .description("Total procedure requests scheduled by patients")
                .register(meterRegistry);

        procedureRequestCanceled = Counter.builder("business.procedure_requests.canceled")
                .description("Total procedure requests canceled")
                .register(meterRegistry);

        paymentSucceeded = Counter.builder("business.payments.succeeded")
                .description("Total successful payments")
                .register(meterRegistry);

        paymentFailed = Counter.builder("business.payments.failed")
                .description("Total failed payments")
                .register(meterRegistry);
    }

    public void recordAppointmentCreated()           { appointmentCreated.increment(); }
    public void recordAppointmentCanceled()          { appointmentCanceled.increment(); }
    public void recordAppointmentRescheduled()       { appointmentRescheduled.increment(); }
    public void recordProcedureRequestCreated()      { procedureRequestCreated.increment(); }
    public void recordProcedureRequestScheduled()    { procedureRequestScheduled.increment(); }
    public void recordProcedureRequestCanceled()     { procedureRequestCanceled.increment(); }
    public void recordPaymentSucceeded()             { paymentSucceeded.increment(); }
    public void recordPaymentFailed()                { paymentFailed.increment(); }
}
