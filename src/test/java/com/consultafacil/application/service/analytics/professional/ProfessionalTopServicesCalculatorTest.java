package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.TopServiceDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalTopServicesCalculatorTest {

    private final ProfessionalTopServicesCalculator calculator = new ProfessionalTopServicesCalculator();

    private ProfessionalService service(String id, String name) {
        return ProfessionalService.builder().id(id).name(name).price(BigDecimal.TEN).build();
    }

    private Appointment booking(ProfessionalService service, AppointmentStatus status,
                                AppointmentPaymentStatus payment, String amount) {
        return Appointment.builder()
                .service(service)
                .status(status)
                .paymentStatus(payment)
                .paymentAmount(amount == null ? null : new BigDecimal(amount))
                .build();
    }

    @Test
    void ranksServicesByBookingsAndSumsPaidRevenue() {
        ProfessionalService consulta = service("s1", "Consulta geral");
        ProfessionalService retorno = service("s2", "Retorno");
        List<TopServiceDTO> top = calculator.calculate(List.of(
                booking(consulta, AppointmentStatus.COMPLETED, AppointmentPaymentStatus.PAID, "200.00"),
                booking(consulta, AppointmentStatus.CONFIRMED, AppointmentPaymentStatus.UNPAID, "200.00"),
                booking(consulta, AppointmentStatus.COMPLETED, AppointmentPaymentStatus.PAID, "180.00"),
                booking(retorno, AppointmentStatus.COMPLETED, AppointmentPaymentStatus.PAID, "100.00"),
                booking(retorno, AppointmentStatus.CANCELED, AppointmentPaymentStatus.REFUNDED, "100.00"),
                booking(null, AppointmentStatus.COMPLETED, AppointmentPaymentStatus.PAID, "50.00")));

        assertThat(top).extracting(TopServiceDTO::name).containsExactly("Consulta geral", "Retorno");
        assertThat(top.get(0).count()).isEqualTo(3);
        assertThat(top.get(0).revenue()).isEqualByComparingTo("380.00");
        assertThat(top.get(1).count()).isEqualTo(1);
        assertThat(top.get(1).revenue()).isEqualByComparingTo("100.00");
    }

    @Test
    void breaksTiesByRevenue() {
        ProfessionalService cheap = service("s1", "Barato");
        ProfessionalService pricey = service("s2", "Caro");
        List<TopServiceDTO> top = calculator.calculate(List.of(
                booking(cheap, AppointmentStatus.COMPLETED, AppointmentPaymentStatus.PAID, "50.00"),
                booking(pricey, AppointmentStatus.COMPLETED, AppointmentPaymentStatus.PAID, "500.00")));

        assertThat(top).extracting(TopServiceDTO::name).containsExactly("Caro", "Barato");
    }

    @Test
    void returnsAtMostFiveServices() {
        List<Appointment> bookings = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            ProfessionalService s = service("s" + i, "Serviço " + i);
            for (int n = 0; n < i; n++) {
                bookings.add(booking(s, AppointmentStatus.COMPLETED, AppointmentPaymentStatus.UNPAID, null));
            }
        }

        List<TopServiceDTO> top = calculator.calculate(bookings);

        assertThat(top).hasSize(5);
        assertThat(top).extracting(TopServiceDTO::name)
                .containsExactly("Serviço 7", "Serviço 6", "Serviço 5", "Serviço 4", "Serviço 3");
        assertThat(top).allSatisfy(s -> assertThat(s.revenue()).isEqualByComparingTo("0"));
    }

    @Test
    void emptyWhenNoAppointmentHasAService() {
        assertThat(calculator.calculate(List.of(
                booking(null, AppointmentStatus.COMPLETED, AppointmentPaymentStatus.PAID, "10.00")))).isEmpty();
    }
}
