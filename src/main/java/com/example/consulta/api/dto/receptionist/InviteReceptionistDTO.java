package com.example.consulta.api.dto.receptionist;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteReceptionistDTO {

    @NotBlank
    @Email
    private String email;
}
