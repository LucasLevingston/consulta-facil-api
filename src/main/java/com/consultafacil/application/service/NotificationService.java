package com.consultafacil.application.service;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicMember;
import com.consultafacil.domain.entity.ClinicMemberId;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.enums.NotificationType;
import com.consultafacil.domain.port.out.ClinicMemberRepositoryPort;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.application.port.in.NotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final ClinicRepositoryPort clinicRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ClinicMemberRepositoryPort clinicMemberRepository;

    @Transactional
    public void sendClinicInvite(String clinicId, String professionalProfileId, String requesterId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("Only the clinic owner can invite professionals");
        }

        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalProfileId));

        if (clinicMemberRepository.existsByClinicIdAndProfessionalProfileId(clinicId, professionalProfileId)) {
            throw new BadRequestException("Professional is already a member of this clinic");
        }

        if (notificationRepository.existsByClinicIdAndProfessionalProfileIdAndStatus(
                clinicId, professionalProfileId, NotificationStatus.PENDING)) {
            throw new BadRequestException("A pending invitation already exists for this professional");
        }

        User targetUser = professional.getUser();

        Notification notification = Notification.builder()
                .type(NotificationType.CLINIC_INVITE)
                .title("Convite para clínica")
                .message("Você foi convidado para fazer parte da clínica " + clinic.getName() + ".")
                .targetUser(targetUser)
                .clinic(clinic)
                .professionalProfile(professional)
                .status(NotificationStatus.PENDING)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getMyNotifications(String userId) {
        return notificationRepository.findByTargetUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(String userId) {
        return notificationRepository.countByTargetUserIdAndStatusIn(userId,
                List.of(NotificationStatus.PENDING));
    }

    @Transactional
    public NotificationResponseDTO markAsRead(String notificationId, String userId) {
        Notification notification = getAndValidate(notificationId, userId);
        if (notification.getStatus() == NotificationStatus.PENDING) {
            notification.setStatus(NotificationStatus.READ);
            notificationRepository.save(notification);
        }
        return toDTO(notification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public NotificationResponseDTO acceptInvite(String notificationId, String userId) {
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

        return toDTO(notification);
    }

    @Transactional
    public NotificationResponseDTO declineInvite(String notificationId, String userId) {
        Notification notification = getAndValidate(notificationId, userId);

        if (notification.getType() != NotificationType.CLINIC_INVITE) {
            throw new BadRequestException("This notification is not a clinic invitation");
        }
        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new BadRequestException("This invitation has already been answered");
        }

        notification.setStatus(NotificationStatus.DECLINED);
        notificationRepository.save(notification);

        return toDTO(notification);
    }

    private Notification getAndValidate(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.getTargetUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to access this notification");
        }
        return notification;
    }

    private NotificationResponseDTO toDTO(Notification n) {
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
