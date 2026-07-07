package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.application.port.in.user.UploadAvatarUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UploadAvatarService implements UploadAvatarUseCase {

    private final UserRepositoryPort userRepository;
    private final StoragePort storagePort;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserResponseDTO execute(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getImageId() != null) {
            storagePort.delete(user.getImageId());
        }
        try {
            String imageUrl = storagePort.upload(file.getBytes(), file.getOriginalFilename(), file.getContentType(), "avatars");
            String imageId = imageUrl.substring(imageUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
            user.updateAvatar(imageUrl, imageId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
        return mapper.toResponseDTO(userRepository.save(user));
    }
}
