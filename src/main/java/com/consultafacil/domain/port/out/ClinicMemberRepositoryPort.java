package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ClinicMember;
import com.consultafacil.domain.entity.ClinicMemberId;

import java.util.List;

public interface ClinicMemberRepositoryPort {

    ClinicMember save(ClinicMember member);

    List<ClinicMember> findByClinicId(String clinicId);

    List<ClinicMember> findByProfessionalProfileId(String professionalProfileId);

    boolean existsByClinicIdAndProfessionalProfileId(String clinicId, String professionalProfileId);

    void deleteById(ClinicMemberId id);
}
