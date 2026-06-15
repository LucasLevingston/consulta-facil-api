package com.consultafacil.api.dto.professional;

public record UpdateAddressDTO(
        String city,
        String state,
        String address,
        String zipCode,
        String neighborhood,
        String streetNumber,
        String complement,
        Double latitude,
        Double longitude
) {}
