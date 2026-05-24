package com.example.consulta.api.dto.appointment;

import com.example.consulta.domain.enums.AppointmentModality;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetModalityDTO {
    @NotNull
    private AppointmentModality modality;
    private String meetLink;
}
