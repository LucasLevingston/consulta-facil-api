package com.consultafacil.application.service.billing;

import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListMyBillingPaymentsServiceTest {

    @Mock BillingPaymentRepositoryPort paymentRepository;

    @Test
    void listMyPayments_returnsList() {
        ListMyBillingPaymentsService service = new ListMyBillingPaymentsService(paymentRepository, new BillingPaymentMapper());
        BillingPayment payment = BillingPayment.builder().id("pay-1").payerId("user-1").build();
        when(paymentRepository.findByPayerId("user-1")).thenReturn(List.of(payment));

        assertThat(service.execute("user-1")).hasSize(1);
    }
}
