package com.consultafacil.api.dto.examlab;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class AvailableSlotDTO {
    private LocalTime time;
    private boolean available;
}
