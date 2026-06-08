package com.consultafacil.api.dto.clinic;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClinicMemberDTO {
    private String professionalProfileId;
    private String professionalName;
    private String specialty;
    private String imageUrl;
    private String role;
}
