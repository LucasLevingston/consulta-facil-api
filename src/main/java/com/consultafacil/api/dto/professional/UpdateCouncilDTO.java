package com.consultafacil.api.dto.professional;

import com.consultafacil.domain.enums.CouncilType;
import jakarta.validation.constraints.Size;

public record UpdateCouncilDTO(
        CouncilType councilType,
        @Size(max = 2) String councilState
) {}
