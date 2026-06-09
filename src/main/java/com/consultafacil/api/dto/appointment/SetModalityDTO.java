package com.consultafacil.api.dto.appointment;

import com.consultafacil.domain.enums.AppointmentModality;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetModalityDTO {
    @NotNull
    private AppointmentModality modality;
    private String meetLink;
}
