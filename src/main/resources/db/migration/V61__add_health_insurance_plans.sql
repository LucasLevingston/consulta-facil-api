CREATE TABLE health_insurance_plans (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_health_insurance_plans PRIMARY KEY (id)
);
CREATE INDEX idx_insurance_plan_code ON health_insurance_plans (code);
CREATE INDEX idx_insurance_plan_active ON health_insurance_plans (active);

INSERT INTO health_insurance_plans (id, name, code, active) VALUES
    ('ins-plan-unimed',       'Unimed Nacional',    'UNIMED',       TRUE),
    ('ins-plan-amil',         'Amil',               'AMIL',         TRUE),
    ('ins-plan-sulamerica',   'SulAmérica Saúde',   'SULAMERICA',   TRUE),
    ('ins-plan-bradesco',     'Bradesco Saúde',      'BRADESCO',     TRUE),
    ('ins-plan-porto-seguro', 'Porto Seguro Saúde',  'PORTO_SEGURO', TRUE);
