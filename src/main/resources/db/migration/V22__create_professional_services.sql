CREATE TABLE professional_services (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    professional_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration_minutes INT NOT NULL,
    requires_consultation BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_ps_professional FOREIGN KEY (professional_id) REFERENCES professional_profiles(id)
);
