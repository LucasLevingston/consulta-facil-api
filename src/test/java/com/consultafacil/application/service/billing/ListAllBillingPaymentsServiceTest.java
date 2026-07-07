package com.consultafacil.application.service.billing;

import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAllBillingPaymentsServiceTest {

    @Mock BillingPaymentRepositoryPort paymentRepository;

    @Test
    void listAll_returnsList() {
        ListAllBillingPaymentsService service = new ListAllBillingPaymentsService(paymentRepository, new BillingPaymentMapper());
        BillingPayment payment = BillingPayment.builder().id("pay-1").build();
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        assertThat(service.execute()).hasSize(1);
    }
}
