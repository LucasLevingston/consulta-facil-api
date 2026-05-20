package com.example.consulta.api.dto.professional;

import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalResponseDTO {
    private String id;
    private String userId;
    private String name;
    private String email;
    private String specialty;
    private String licenseNumber;
    private String phone;
    private String imageUrl;
    private Double rating;
    private Integer consultationCount;
    private ProfessionalProfileStatus status;
    private String city;
    private String state;
    private String address;
    private Double latitude;
    private Double longitude;
    private String clinicId;
    private String clinicName;
}
