package com.consultafacil.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicMemberId implements Serializable {

    @Column(name = "clinic_id")
    private String clinicId;

    @Column(name = "professional_profile_id")
    private String professionalProfileId;
}
