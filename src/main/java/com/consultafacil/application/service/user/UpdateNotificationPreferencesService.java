package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.UpdateNotificationPreferencesDTO;
import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.application.port.in.user.UpdateNotificationPreferencesUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateNotificationPreferencesService implements UpdateNotificationPreferencesUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDTO execute(String userId, UpdateNotificationPreferencesDTO preferences) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setWhatsappNotificationsEnabled(preferences.getWhatsappNotificationsEnabled());
        if (preferences.getEmailNotificationsEnabled() != null) {
            user.setEmailNotificationsEnabled(preferences.getEmailNotificationsEnabled());
        }
        return userMapper.toResponseDTO(userRepository.save(user));
    }
}
