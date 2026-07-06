package com.consultafacil.application.service;

import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.entity.ClinicMemberId;
import com.consultafacil.domain.port.out.ClinicMemberRepositoryPort;
import com.consultafacil.domain.port.out.ClinicRepositoryPort;
import com.consultafacil.application.port.in.RemoveClinicMemberUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveClinicMemberService implements RemoveClinicMemberUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicMemberRepositoryPort clinicMemberRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void execute(String clinicId, String professionalProfileId, String requesterId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(requesterId)) {
            throw new BadRequestException("Only the clinic owner can remove members");
        }

        ClinicMemberId id = new ClinicMemberId(clinicId, professionalProfileId);
        clinicMemberRepository.deleteById(id);
        em.flush();
        em.clear();
    }
}
