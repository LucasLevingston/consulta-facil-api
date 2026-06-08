package com.consultafacil.api.dto.clinic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClinicDTO {

    @NotBlank
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
}
