package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.professional.profile.GetAllProfessionalsUseCase;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAllProfessionalsService implements GetAllProfessionalsUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileSummaryMapper summaryMapper;

    @Override
    public Page<ProfessionalResponseDTO> execute(String profession, String specialty, String name, Pageable pageable) {
        String profParam = resolveEnumName(ProfessionalType.class, profession);
        String specParam = resolveEnumName(Specialty.class, specialty);
        String nameParam = (name != null && !name.isBlank()) ? name : "";
        return professionalProfileRepository.findActiveWithFilters(profParam, specParam, nameParam, pageable)
                .map(summaryMapper::toListSummaryDTO);
    }

    private <T extends Enum<T>> String resolveEnumName(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) return "";
        try {
            return Enum.valueOf(enumClass, value.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
