package com.consultafacil.application.port.in.auth;

import com.consultafacil.api.dto.auth.LoginResponseDTO;

public interface RotateRefreshTokenUseCase {
    LoginResponseDTO execute(String tokenValue);
}
