package com.consultafacil.application.port.in.user;

import com.consultafacil.api.dto.user.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadAvatarUseCase {
    UserResponseDTO execute(String userId, MultipartFile file);
}
