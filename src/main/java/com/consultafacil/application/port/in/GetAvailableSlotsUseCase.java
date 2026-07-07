package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.examlab.AvailableSlotDTO;

import java.time.LocalDate;
import java.util.List;

public interface GetAvailableSlotsUseCase {
    List<AvailableSlotDTO> execute(String examLabId, LocalDate date);
}
