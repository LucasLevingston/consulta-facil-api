package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.ClinicMember;
import com.example.consulta.domain.entity.ClinicMemberId;

import java.util.List;

public interface ClinicMemberRepositoryPort {

    ClinicMember save(ClinicMember member);

    List<ClinicMember> findByClinicId(String clinicId);

    List<ClinicMember> findByProfessionalProfileId(String professionalProfileId);

    boolean existsByClinicIdAndProfessionalProfileId(String clinicId, String professionalProfileId);

    void deleteById(ClinicMemberId id);
}
