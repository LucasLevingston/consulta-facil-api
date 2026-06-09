package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.api.dto.user.UserResponseDTO;

public interface RegisterUserUseCase {

    UserResponseDTO execute(CreateUserDTO dto);
}
