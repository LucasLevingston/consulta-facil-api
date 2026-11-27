package com.consultafacil.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalInsurancePK implements Serializable {

    private String professionalId;
    private String planId;
}
