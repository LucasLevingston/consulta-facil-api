package com.consultafacil.api.dto.examlab;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateExamLabDTO {

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
    private List<String> acceptedExams;
}
