package com.consultafacil.application.service;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicMember;
import com.consultafacil.domain.entity.ClinicMemberId;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.enums.NotificationType;
import com.consultafacil.domain.port.out.ClinicMemberRepositoryPort;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.application.port.in.AcceptInviteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcceptInviteService implements AcceptInviteUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final ClinicMemberRepositoryPort clinicMemberRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationResponseDTO execute(String notificationId, String userId) {
        Notification notification = getAndValidate(notificationId, userId);

        if (notification.getType() != NotificationType.CLINIC_INVITE) {
            throw new BadRequestException("This notification is not a clinic invitation");
        }
        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new BadRequestException("This invitation has already been answered");
        }

        Clinic clinic = notification.getClinic();
        ProfessionalProfile professional = notification.getProfessionalProfile();

        if (clinicMemberRepository.existsByClinicIdAndProfessionalProfileId(clinic.getId(), professional.getId())) {
            throw new BadRequestException("Professional is already a member of this clinic");
        }

        ClinicMember member = ClinicMember.builder()
                .id(new ClinicMemberId(clinic.getId(), professional.getId()))
                .clinic(clinic)
                .professionalProfile(professional)
                .role("MEMBER")
                .build();
        clinicMemberRepository.save(member);

        notification.setStatus(NotificationStatus.ACCEPTED);
        notificationRepository.save(notification);

        return notificationMapper.toResponseDTO(notification);
    }

    private Notification getAndValidate(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.getTargetUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to access this notification");
        }
        return notification;
    }
}
