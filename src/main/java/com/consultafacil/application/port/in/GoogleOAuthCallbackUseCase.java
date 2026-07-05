package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.auth.LoginResponseDTO;

public interface GoogleOAuthCallbackUseCase {
    LoginResponseDTO execute(String code);
}
