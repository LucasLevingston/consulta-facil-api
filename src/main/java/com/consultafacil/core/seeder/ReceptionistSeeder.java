package com.consultafacil.core.seeder;

import com.consultafacil.api.dto.receptionist.InviteReceptionistDTO;
import com.consultafacil.application.service.clinic.InviteReceptionistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceptionistSeeder {

    private final TestUserSeeder testUserSeeder;
    private final InviteReceptionistService inviteReceptionistService;

    public void seed(String email, String password, String name, String cpf, String clinicId, String ownerUserId) {
        try {
            testUserSeeder.createPatient(email, password, name, cpf, "https://i.pravatar.cc/150?img=5");
            InviteReceptionistDTO dto = new InviteReceptionistDTO();
            dto.setEmail(email);
            inviteReceptionistService.execute(clinicId, ownerUserId, dto);
            log.info("Recepcionista criada: {}", email);
        } catch (Exception e) {
            log.warn("Erro ao criar recepcionista: {}", e.getMessage());
        }
    }
}
