package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.domain.enums.UserRole;
import org.springframework.data.domain.Page;

public interface GetAllUsersUseCase {
    Page<UserResponseDTO> execute(int page, int size, UserRole role);
}
