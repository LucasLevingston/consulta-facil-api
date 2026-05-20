package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.ClinicMember;
import com.example.consulta.domain.entity.ClinicMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicMemberRepository extends JpaRepository<ClinicMember, ClinicMemberId> {

    List<ClinicMember> findByClinicId(String clinicId);

    List<ClinicMember> findByProfessionalProfileId(String professionalProfileId);

    boolean existsByClinicIdAndProfessionalProfileId(String clinicId, String professionalProfileId);
}
