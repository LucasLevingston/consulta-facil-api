package com.example.consulta.api.dto.auth;

import com.example.consulta.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String refreshToken;
    private String type;
    private Long expiresIn;
    private String userId;
    private String email;
    private UserRole role;

    public static LoginResponseDTO of(String token, Long expiresIn, String userId, String email, UserRole role) {
        return LoginResponseDTO.builder()
            .token(token)
            .type("Bearer")
            .expiresIn(expiresIn)
            .userId(userId)
            .email(email)
            .role(role)
            .build();
    }

    public static LoginResponseDTO of(String token, String refreshToken, Long expiresIn,
                                      String userId, String email, UserRole role) {
        return LoginResponseDTO.builder()
            .token(token)
            .refreshToken(refreshToken)
            .type("Bearer")
            .expiresIn(expiresIn)
            .userId(userId)
            .email(email)
            .role(role)
            .build();
    }
}
