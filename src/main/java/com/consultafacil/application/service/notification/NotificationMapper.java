package com.consultafacil.application.service.notification;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;
import com.consultafacil.domain.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponseDTO toResponseDTO(Notification n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .status(n.getStatus())
                .clinicId(n.getClinic() != null ? n.getClinic().getId() : null)
                .clinicName(n.getClinic() != null ? n.getClinic().getName() : null)
                .professionalProfileId(n.getProfessionalProfile() != null ? n.getProfessionalProfile().getId() : null)
                .createdAt(n.getCreatedAt())
                .build();
    }
}
