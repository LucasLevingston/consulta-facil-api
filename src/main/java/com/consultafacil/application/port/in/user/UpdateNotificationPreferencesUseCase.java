package com.consultafacil.application.port.in.user;

import com.consultafacil.api.dto.user.UpdateNotificationPreferencesDTO;
import com.consultafacil.api.dto.user.UserResponseDTO;

public interface UpdateNotificationPreferencesUseCase {
    UserResponseDTO execute(String userId, UpdateNotificationPreferencesDTO preferences);
}
