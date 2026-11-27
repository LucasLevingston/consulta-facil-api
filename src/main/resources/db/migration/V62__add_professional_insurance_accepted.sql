CREATE TABLE professional_insurance_accepted (
    professional_id VARCHAR(36) NOT NULL,
    plan_id VARCHAR(36) NOT NULL,
    notes VARCHAR(300),
    CONSTRAINT pk_professional_insurance PRIMARY KEY (professional_id, plan_id),
    CONSTRAINT fk_prof_insurance_professional FOREIGN KEY (professional_id) REFERENCES professional_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_prof_insurance_plan FOREIGN KEY (plan_id) REFERENCES health_insurance_plans(id) ON DELETE CASCADE
);
