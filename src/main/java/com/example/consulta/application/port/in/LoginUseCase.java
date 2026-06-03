package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.auth.LoginResponseDTO;

public interface LoginUseCase {

    LoginResponseDTO execute(LoginRequestDTO request);
}
