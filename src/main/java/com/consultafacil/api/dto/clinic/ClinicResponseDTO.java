package com.consultafacil.api.dto.clinic;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClinicResponseDTO {

    private String id;
    private String name;
    private String description;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String status;
    private String ownerId;
    private String ownerName;
    private List<ClinicMemberDTO> members;
    private LocalDateTime createdAt;
}
