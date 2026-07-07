package com.consultafacil.adapter.out.persistence.clinic;

import com.consultafacil.domain.entity.ClinicMember;
import com.consultafacil.domain.entity.ClinicMemberId;
import com.consultafacil.domain.port.out.clinic.ClinicMemberRepositoryPort;
import com.consultafacil.domain.repository.clinic.ClinicMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ClinicMemberRepositoryAdapter implements ClinicMemberRepositoryPort {

    private final ClinicMemberRepository clinicMemberRepository;

    @Override
    public ClinicMember save(ClinicMember member) {
        return clinicMemberRepository.save(member);
    }

    @Override
    public List<ClinicMember> findByClinicId(String clinicId) {
        return clinicMemberRepository.findByClinicId(clinicId);
    }

    @Override
    public List<ClinicMember> findByProfessionalProfileId(String professionalProfileId) {
        return clinicMemberRepository.findByProfessionalProfileId(professionalProfileId);
    }

    @Override
    public boolean existsByClinicIdAndProfessionalProfileId(String clinicId, String professionalProfileId) {
        return clinicMemberRepository.existsByClinicIdAndProfessionalProfileId(clinicId, professionalProfileId);
    }

    @Override
    public void deleteById(ClinicMemberId id) {
        clinicMemberRepository.deleteById(id);
    }
}
