package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.TopServiceDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ProfessionalService;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Services ranked by non-canceled bookings (then by PAID revenue), top 5. */
@Component
public class ProfessionalTopServicesCalculator {

    private static final int LIMIT = 5;

    public List<TopServiceDTO> calculate(List<Appointment> appointments) {
        Map<String, TopServiceDTO> byService = new LinkedHashMap<>();
        for (Appointment a : appointments) {
            ProfessionalService service = a.getService();
            if (service == null || a.getStatus() == AppointmentStatus.CANCELED) continue;
            BigDecimal revenue = a.getPaymentStatus() == AppointmentPaymentStatus.PAID && a.getPaymentAmount() != null
                    ? a.getPaymentAmount() : BigDecimal.ZERO;
            byService.merge(service.getId(), new TopServiceDTO(service.getName(), 1, revenue),
                    (acc, one) -> new TopServiceDTO(acc.name(), acc.count() + 1, acc.revenue().add(one.revenue())));
        }
        return byService.values().stream()
                .sorted(Comparator.comparingLong(TopServiceDTO::count).reversed()
                        .thenComparing(TopServiceDTO::revenue, Comparator.reverseOrder())
                        .thenComparing(TopServiceDTO::name, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(LIMIT)
                .toList();
    }
}
