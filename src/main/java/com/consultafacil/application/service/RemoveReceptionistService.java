package com.consultafacil.application.service;

import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicReceptionist;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.ClinicReceptionistRepositoryPort;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.application.port.in.RemoveReceptionistUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveReceptionistService implements RemoveReceptionistUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicReceptionistRepositoryPort clinicReceptionistRepository;
    private final UserRepositoryPort userRepository;

    @Transactional
    public void execute(String clinicId, String receptionistId, String ownerUserId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(ownerUserId)) {
            throw new BadRequestException("Only clinic owner can remove receptionists");
        }

        ClinicReceptionist entry = clinicReceptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new ResourceNotFoundException("ClinicReceptionist", receptionistId));

        if (!entry.getClinic().getId().equals(clinicId)) {
            throw new BadRequestException("Receptionist does not belong to this clinic");
        }

        User user = entry.getUser();
        clinicReceptionistRepository.delete(entry);

        if (!clinicReceptionistRepository.existsByUserId(user.getId())) {
            user.setRole(UserRole.PATIENT);
            userRepository.save(user);
        }
    }
}
