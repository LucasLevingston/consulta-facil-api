package com.example.consulta.api.dto.user;

import com.example.consulta.domain.enums.Gender;
import com.example.consulta.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private String id;
    private String name;
    private String email;
    private UserRole role;
    private String phone;
    private String cpf;
    private LocalDate birthDate;
    private Gender gender;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
