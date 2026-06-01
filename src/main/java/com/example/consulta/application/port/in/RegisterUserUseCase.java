package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.api.dto.user.UserResponseDTO;

public interface RegisterUserUseCase {

    UserResponseDTO execute(CreateUserDTO dto);
}
