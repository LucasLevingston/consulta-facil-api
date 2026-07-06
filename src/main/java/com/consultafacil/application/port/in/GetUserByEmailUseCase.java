package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.user.UserResponseDTO;

public interface GetUserByEmailUseCase {
    UserResponseDTO execute(String email);
}
