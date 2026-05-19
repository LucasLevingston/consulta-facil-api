package com.example.consulta.api.dto.notification;

import com.example.consulta.domain.enums.NotificationStatus;
import com.example.consulta.domain.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponseDTO {
    private String id;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationStatus status;
    private String clinicId;
    private String clinicName;
    private String doctorProfileId;
    private LocalDateTime createdAt;
}
