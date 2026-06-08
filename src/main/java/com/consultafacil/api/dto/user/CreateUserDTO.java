package com.consultafacil.api.dto.user;

import com.consultafacil.domain.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDTO {
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 256, message = "Senha deve ter entre 8 e 256 caracteres")
    private String password;

    @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter 11 dígitos")
    private String cpf;

    @Pattern(regexp = "^(\\d{10,11}|\\(\\d{2}\\) \\d{4,5}-\\d{4})$", message = "Telefone deve conter 10 ou 11 dígitos, ou estar no formato (XX) XXXXX-XXXX")
    private String phone;

    private LocalDate birthDate;

    private String imageUrl;

    private Gender gender;
}
