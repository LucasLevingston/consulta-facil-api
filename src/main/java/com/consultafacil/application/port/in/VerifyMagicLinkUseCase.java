package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.auth.LoginResponseDTO;

public interface VerifyMagicLinkUseCase {
    LoginResponseDTO execute(String token);
}
