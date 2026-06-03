package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.auth.LoginResponseDTO;

public interface GoogleLoginUseCase {
    LoginResponseDTO execute(String idToken);
}
