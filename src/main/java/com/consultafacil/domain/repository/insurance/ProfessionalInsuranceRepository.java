package com.consultafacil.domain.repository.insurance;

import com.consultafacil.domain.entity.ProfessionalInsuranceAccepted;
import com.consultafacil.domain.entity.ProfessionalInsurancePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessionalInsuranceRepository extends JpaRepository<ProfessionalInsuranceAccepted, ProfessionalInsurancePK> {

    List<ProfessionalInsuranceAccepted> findByProfessionalId(String professionalId);

    void deleteByProfessionalIdAndPlanId(String professionalId, String planId);

    @Query("SELECT p.professionalId FROM ProfessionalInsuranceAccepted p WHERE p.planId = :planId")
    List<String> findProfessionalIdByPlanId(@Param("planId") String planId);
}
