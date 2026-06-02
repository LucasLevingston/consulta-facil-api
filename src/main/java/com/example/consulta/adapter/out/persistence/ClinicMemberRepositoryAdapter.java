package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.ClinicMember;
import com.example.consulta.domain.entity.ClinicMemberId;
import com.example.consulta.domain.port.out.ClinicMemberRepositoryPort;
import com.example.consulta.domain.repository.ClinicMemberRepository;
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
