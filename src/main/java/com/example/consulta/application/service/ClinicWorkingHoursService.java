package com.example.consulta.application.service;

import com.example.consulta.api.dto.schedule.ClinicWorkingHoursResponseDTO;
import com.example.consulta.api.dto.schedule.CreateClinicWorkingHoursDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ClinicWorkingHours;
import com.example.consulta.domain.port.out.ClinicRepositoryPort;
import com.example.consulta.domain.port.out.ClinicWorkingHoursRepositoryPort;
import com.example.consulta.application.port.in.ClinicWorkingHoursUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicWorkingHoursService implements ClinicWorkingHoursUseCase {

    private final ClinicWorkingHoursRepositoryPort workingHoursRepository;
    private final ClinicRepositoryPort clinicRepository;

    @Transactional(readOnly = true)
    public List<ClinicWorkingHoursResponseDTO> getClinicWorkingHours(String clinicId) {
        log.debug("Fetching working hours for clinic: {}", clinicId);
        return workingHoursRepository.findByClinicId(clinicId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public List<ClinicWorkingHoursResponseDTO> saveClinicWorkingHours(
            String clinicId, String userId, List<CreateClinicWorkingHoursDTO> dtos) {
        log.debug("Saving working hours for clinic: {}", clinicId);
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(userId)) {
            throw new BadRequestException("You are not the owner of this clinic");
        }

        List<ClinicWorkingHours> saved = dtos.stream().map(dto -> {
            ClinicWorkingHours wh = workingHoursRepository
                    .findByClinicIdAndDayOfWeek(clinicId, dto.getDayOfWeek())
                    .orElse(ClinicWorkingHours.builder()
                            .clinic(clinic)
                            .dayOfWeek(dto.getDayOfWeek())
                            .build());

            wh.setOpenTime(dto.getOpenTime());
            wh.setCloseTime(dto.getCloseTime());
            if (dto.getIsOpen() != null) {
                wh.setIsOpen(dto.getIsOpen());
            }
            return workingHoursRepository.save(wh);
        }).toList();

        return saved.stream().map(this::toDTO).toList();
    }

    @Override
    public List<ClinicWorkingHoursResponseDTO> getWorkingHours(String clinicId) { return getClinicWorkingHours(clinicId); }

    @Override
    public List<ClinicWorkingHoursResponseDTO> saveWorkingHours(String clinicId, String userId, List<CreateClinicWorkingHoursDTO> dtos) { return saveClinicWorkingHours(clinicId, userId, dtos); }

    private ClinicWorkingHoursResponseDTO toDTO(ClinicWorkingHours wh) {
        return ClinicWorkingHoursResponseDTO.builder()
                .id(wh.getId())
                .clinicId(wh.getClinic().getId())
                .dayOfWeek(wh.getDayOfWeek())
                .openTime(wh.getOpenTime())
                .closeTime(wh.getCloseTime())
                .isOpen(wh.getIsOpen())
                .build();
    }
}
