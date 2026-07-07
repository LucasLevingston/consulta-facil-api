package com.consultafacil.core.seeder;

import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.enums.NotificationType;
import com.consultafacil.domain.repository.notification.NotificationRepository;
import com.consultafacil.domain.repository.user.UserRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSeeder {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final Faker faker = new Faker(new Locale("pt-BR"));

    private record NotifTemplate(NotificationType type, String title, String message) {
    }

    public void seed(List<String> patientUserIds, List<String> professionalProfileIds) {
        List<NotifTemplate> templates = List.of(
                new NotifTemplate(NotificationType.APPOINTMENT_SCHEDULED,
                        "Consulta agendada", "Sua consulta foi agendada com sucesso."),
                new NotifTemplate(NotificationType.APPOINTMENT_CONFIRMED,
                        "Consulta confirmada", "Sua consulta foi confirmada pelo profissional."),
                new NotifTemplate(NotificationType.APPOINTMENT_CANCELED,
                        "Consulta cancelada", "Sua consulta foi cancelada."),
                new NotifTemplate(NotificationType.GENERAL,
                        "Resultado disponível", "O resultado do seu exame está disponível."));

        List<NotificationStatus> statuses = List.of(
                NotificationStatus.READ, NotificationStatus.READ,
                NotificationStatus.PENDING, NotificationStatus.PENDING);

        int created = 0;
        for (String userId : patientUserIds) {
            int count = faker.random().nextInt(2, 6);
            for (int i = 0; i < count; i++) {
                try {
                    userRepository.findById(userId).ifPresent(user -> {
                        NotifTemplate t = templates.get(faker.random().nextInt(templates.size()));
                        notificationRepository.save(Notification.builder()
                                .targetUser(user)
                                .type(t.type())
                                .title(t.title())
                                .message(t.message())
                                .status(statuses.get(faker.random().nextInt(statuses.size())))
                                .build());
                    });
                    created++;
                } catch (Exception e) {
                    log.debug("Erro ao criar notification: {}", e.getMessage());
                }
            }
        }
        log.info("[Seed] Notifications criadas: {}", created);
    }
}
