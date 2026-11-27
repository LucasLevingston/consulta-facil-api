package com.consultafacil.application.service.prescription;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Prescription;
import com.consultafacil.domain.port.out.prescription.PrescriptionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadPrescriptionServiceTest {

    @Mock PrescriptionRepositoryPort prescriptionRepository;

    @InjectMocks DownloadPrescriptionService service;

    @Test
    void download_found_returnsPdfUrl() {
        Prescription prescription = Prescription.builder().id("rx-1").pdfUrl("https://s3.example.com/rx.pdf").build();
        when(prescriptionRepository.findById("rx-1")).thenReturn(Optional.of(prescription));

        String url = service.execute("user-1", "rx-1");

        assertThat(url).isEqualTo("https://s3.example.com/rx.pdf");
    }

    @Test
    void download_notFound_throwsResourceNotFoundException() {
        when(prescriptionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("user-1", "missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
