package com.consultafacil.application.port.in.auth;

import com.consultafacil.api.dto.auth.LoginResponseDTO;

public interface VerifyMagicLinkUseCase {
    LoginResponseDTO execute(String token);
}
