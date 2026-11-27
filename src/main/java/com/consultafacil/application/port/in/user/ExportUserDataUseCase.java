package com.consultafacil.application.port.in.user;

import com.consultafacil.api.dto.user.UserDataExportDTO;

public interface ExportUserDataUseCase {
    UserDataExportDTO execute(String userId);
}
