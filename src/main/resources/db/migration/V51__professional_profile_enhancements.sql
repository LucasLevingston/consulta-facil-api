-- Conselho profissional
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS council_type  VARCHAR(20);
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS council_state VARCHAR(2);

-- Facebook (social links V47 already has instagram/linkedin/website)
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS facebook_url VARCHAR(255);

-- Endereço estruturado
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS zip_code      VARCHAR(9);
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS neighborhood  VARCHAR(100);
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS street_number VARCHAR(20);
ALTER TABLE professional_profiles ADD COLUMN IF NOT EXISTS complement    VARCHAR(100);

-- Formação acadêmica
CREATE TABLE IF NOT EXISTS professional_education (
    id                      VARCHAR(36)  PRIMARY KEY,
    professional_profile_id VARCHAR(36)  NOT NULL REFERENCES professional_profiles(id) ON DELETE CASCADE,
    degree                  VARCHAR(20)  NOT NULL,
    institution             VARCHAR(255) NOT NULL,
    field_of_study          VARCHAR(255),
    graduation_year         INTEGER,
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_education_professional ON professional_education(professional_profile_id);

-- Experiência profissional
CREATE TABLE IF NOT EXISTS professional_experience (
    id                      VARCHAR(36)  PRIMARY KEY,
    professional_profile_id VARCHAR(36)  NOT NULL REFERENCES professional_profiles(id) ON DELETE CASCADE,
    position                VARCHAR(255) NOT NULL,
    institution             VARCHAR(255) NOT NULL,
    start_year              INTEGER      NOT NULL,
    end_year                INTEGER,
    description             TEXT,
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_experience_professional ON professional_experience(professional_profile_id);

-- Certificados
CREATE TABLE IF NOT EXISTS professional_certificates (
    id                      VARCHAR(36)  PRIMARY KEY,
    professional_profile_id VARCHAR(36)  NOT NULL REFERENCES professional_profiles(id) ON DELETE CASCADE,
    title                   VARCHAR(255) NOT NULL,
    issuing_organization    VARCHAR(255),
    issue_year              INTEGER,
    certificate_url         VARCHAR(500),
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_certificates_professional ON professional_certificates(professional_profile_id);
