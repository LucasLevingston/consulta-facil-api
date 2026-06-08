package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.auth.LoginResponseDTO;

public interface LoginUseCase {

    LoginResponseDTO execute(LoginRequestDTO request);
}
