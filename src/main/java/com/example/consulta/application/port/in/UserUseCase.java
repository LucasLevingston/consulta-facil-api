package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.user.CreateUserDTO;
import com.example.consulta.api.dto.user.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserUseCase {

    UserResponseDTO createUser(CreateUserDTO dto);

    UserResponseDTO getById(String id);

    UserResponseDTO getByEmail(String email);

    UserResponseDTO uploadAvatar(String userId, MultipartFile file);

    void deleteUser(String id);
}
