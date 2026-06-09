package com.consultafacil.api.dto.appointment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrCheckInTokenDTO {
    private String appointmentId;
    private String token;
}
