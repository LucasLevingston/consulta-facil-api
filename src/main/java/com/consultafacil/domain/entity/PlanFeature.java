package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_features")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeature {

    @EmbeddedId
    private PlanFeatureId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("planId")
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("featureId")
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @Column(name = "feature_value", nullable = false, length = 100)
    @Builder.Default
    private String value = "true";
}
