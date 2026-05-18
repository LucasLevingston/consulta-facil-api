package com.example.consulta.api.dto.subscription;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponseDTO {
    private String checkoutUrl;
    private String preferenceId;
}
