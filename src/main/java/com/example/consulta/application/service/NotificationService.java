package com.example.consulta.application.service;

import com.example.consulta.api.dto.notification.NotificationResponseDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ClinicMember;
import com.example.consulta.domain.entity.ClinicMemberId;
import com.example.consulta.domain.entity.DoctorProfile;
import com.example.consulta.domain.entity.Notification;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.NotificationStatus;
import com.example.consulta.domain.enums.NotificationType;
import com.example.consulta.domain.repository.ClinicMemberRepository;
import com.example.consulta.domain.repository.ClinicRepository;
import com.example.consulta.domain.repository.DoctorProfileRepository;
import com.example.consulta.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ClinicMemberRepository clinicMemberRepository;

    @Transactional
    public void sendClinicInvite(String clinicId, String doctorProfileId, String requesterId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("Apenas o proprietário pode convidar médicos");
        }

        DoctorProfile doctor = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorProfileId));

        if (clinicMemberRepository.existsByClinicIdAndDoctorProfileId(clinicId, doctorProfileId)) {
            throw new BadRequestException("Médico já é membro desta clínica");
        }

        if (notificationRepository.existsByClinicIdAndDoctorProfileIdAndStatus(
                clinicId, doctorProfileId, NotificationStatus.PENDING)) {
            throw new BadRequestException("Já existe um convite pendente para este médico");
        }

        User targetUser = doctor.getUser();

        Notification notification = Notification.builder()
                .type(NotificationType.CLINIC_INVITE)
                .title("Convite para clínica")
                .message("Você foi convidado para fazer parte da clínica " + clinic.getName() + ".")
                .targetUser(targetUser)
                .clinic(clinic)
                .doctorProfile(doctor)
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
        DoctorProfile doctor = notification.getDoctorProfile();

        if (clinicMemberRepository.existsByClinicIdAndDoctorProfileId(clinic.getId(), doctor.getId())) {
            throw new BadRequestException("Médico já é membro desta clínica");
        }

        ClinicMember member = ClinicMember.builder()
                .id(new ClinicMemberId(clinic.getId(), doctor.getId()))
                .clinic(clinic)
                .doctorProfile(doctor)
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
                .doctorProfileId(n.getDoctorProfile() != null ? n.getDoctorProfile().getId() : null)
                .createdAt(n.getCreatedAt())
                .build();
    }
}
