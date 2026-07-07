package com.consultafacil.domain.repository.clinic;

import com.consultafacil.domain.entity.ClinicMember;
import com.consultafacil.domain.entity.ClinicMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicMemberRepository extends JpaRepository<ClinicMember, ClinicMemberId> {

    List<ClinicMember> findByClinicId(String clinicId);

    List<ClinicMember> findByProfessionalProfileId(String professionalProfileId);

    boolean existsByClinicIdAndProfessionalProfileId(String clinicId, String professionalProfileId);
}
