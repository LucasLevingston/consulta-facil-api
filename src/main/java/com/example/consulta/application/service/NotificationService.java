package com.example.consulta.application.service;

import com.example.consulta.api.dto.notification.NotificationResponseDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ClinicMember;
import com.example.consulta.domain.entity.ClinicMemberId;
import com.example.consulta.domain.entity.ProfessionalProfile;
import com.example.consulta.domain.entity.Notification;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.NotificationStatus;
import com.example.consulta.domain.enums.NotificationType;
import com.example.consulta.domain.repository.ClinicMemberRepository;
import com.example.consulta.domain.repository.ClinicRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.domain.repository.NotificationRepository;
import com.example.consulta.application.port.in.NotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {

    private final NotificationRepository notificationRepository;
    private final ClinicRepository clinicRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final ClinicMemberRepository clinicMemberRepository;

    @Transactional
    public void sendClinicInvite(String clinicId, String professionalProfileId, String requesterId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("Apenas o proprietário pode convidar profissionais");
        }

        ProfessionalProfile professional = professionalProfileRepository.findById(professionalProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional", professionalProfileId));

        if (clinicMemberRepository.existsByClinicIdAndProfessionalProfileId(clinicId, professionalProfileId)) {
            throw new BadRequestException("Profissional já é membro desta clínica");
        }

        if (notificationRepository.existsByClinicIdAndProfessionalProfileIdAndStatus(
                clinicId, professionalProfileId, NotificationStatus.PENDING)) {
            throw new BadRequestException("Já existe um convite pendente para este profissional");
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
            throw new BadRequestException("Esta notificação não é um convite de clínica");
        }
        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new BadRequestException("Este convite já foi respondido");
        }

        Clinic clinic = notification.getClinic();
        ProfessionalProfile professional = notification.getProfessionalProfile();

        if (clinicMemberRepository.existsByClinicIdAndProfessionalProfileId(clinic.getId(), professional.getId())) {
            throw new BadRequestException("Profissional já é membro desta clínica");
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
            throw new BadRequestException("Esta notificação não é um convite de clínica");
        }
        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new BadRequestException("Este convite já foi respondido");
        }

        notification.setStatus(NotificationStatus.DECLINED);
        notificationRepository.save(notification);

        return toDTO(notification);
    }

    private Notification getAndValidate(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.getTargetUser().getId().equals(userId)) {
            throw new BadRequestException("Você não tem permissão para acessar esta notificação");
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
