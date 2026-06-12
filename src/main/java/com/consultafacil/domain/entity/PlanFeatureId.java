package com.consultafacil.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlanFeatureId implements Serializable {
    private String planId;
    private String featureId;
}
