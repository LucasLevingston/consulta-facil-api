package com.example.consulta.application.service;

import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Clinic;
import com.example.consulta.domain.entity.ClinicReceptionist;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.repository.ClinicReceptionistRepository;
import com.example.consulta.domain.repository.ClinicRepository;
import com.example.consulta.domain.repository.UserRepository;
import com.example.consulta.application.port.in.RemoveReceptionistUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveReceptionistService implements RemoveReceptionistUseCase {

    private final ClinicRepository clinicRepository;
    private final ClinicReceptionistRepository clinicReceptionistRepository;
    private final UserRepository userRepository;

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
