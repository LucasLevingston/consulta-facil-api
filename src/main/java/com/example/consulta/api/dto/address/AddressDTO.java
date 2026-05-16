package com.example.consulta.api.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "^\\d{8}$", message = "CEP deve conter 8 dígitos")
    private String zipCode;

    @NotBlank(message = "Rua é obrigatória")
    private String street;

    @NotBlank(message = "Número é obrigatório")
    private String number;

    private String district;

    @NotBlank(message = "Cidade é obrigatória")
    private String city;

    @NotBlank(message = "Estado é obrigatório")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve conter 2 letras maiúsculas")
    private String state;

    private String country;
}
