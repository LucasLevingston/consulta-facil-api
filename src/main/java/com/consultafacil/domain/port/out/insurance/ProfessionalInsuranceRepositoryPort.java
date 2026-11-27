package com.consultafacil.domain.port.out.insurance;

import com.consultafacil.domain.entity.ProfessionalInsuranceAccepted;

import java.util.List;

public interface ProfessionalInsuranceRepositoryPort {

    ProfessionalInsuranceAccepted save(ProfessionalInsuranceAccepted entity);

    List<ProfessionalInsuranceAccepted> findByProfessionalId(String professionalId);

    void deleteByProfessionalIdAndPlanId(String professionalId, String planId);
}
