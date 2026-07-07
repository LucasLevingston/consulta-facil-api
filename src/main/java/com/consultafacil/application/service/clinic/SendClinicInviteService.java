package com.consultafacil.application.service.clinic;

import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.enums.NotificationType;
import com.consultafacil.domain.port.out.ClinicMemberRepositoryPort;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.application.port.in.SendClinicInviteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendClinicInviteService implements SendClinicInviteUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final ClinicRepositoryPort clinicRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ClinicMemberRepositoryPort clinicMemberRepository;

    @Transactional
    public void execute(String clinicId, String professionalProfileId, String requesterId) {
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
}
