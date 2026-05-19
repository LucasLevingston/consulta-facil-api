package com.example.consulta.api.dto.doctor;

import com.example.consulta.domain.enums.DoctorProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponseDTO {
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
    private DoctorProfileStatus status;
}
