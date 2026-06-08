package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.user.CreateUserDTO;
import com.consultafacil.api.dto.user.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserUseCase {

    UserResponseDTO createUser(CreateUserDTO dto);

    UserResponseDTO getById(String id);

    UserResponseDTO getByEmail(String email);

    UserResponseDTO uploadAvatar(String userId, MultipartFile file);

    void deleteUser(String id);
}
