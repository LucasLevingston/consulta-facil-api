-- Features
CREATE TABLE features (
    id VARCHAR(36) PRIMARY KEY,
    feature_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- PlanFeature join table
CREATE TABLE plan_features (
    plan_id VARCHAR(36) NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    feature_id VARCHAR(36) NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    feature_value VARCHAR(100) NOT NULL DEFAULT 'true',
    PRIMARY KEY (plan_id, feature_id)
);

-- Generalized payment record (saves exact values at charge time)
CREATE TABLE billing_payments (
    id VARCHAR(36) PRIMARY KEY,
    payment_type VARCHAR(30) NOT NULL,
    reference_id VARCHAR(36),
    owner_type VARCHAR(20),
    owner_id VARCHAR(36),
    amount DECIMAL(12,2) NOT NULL,
    system_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    gateway_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    payment_method VARCHAR(30),
    gateway VARCHAR(30) NOT NULL DEFAULT 'MOCK',
    gateway_payment_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payer_id VARCHAR(36),
    payer_name VARCHAR(150),
    payer_email VARCHAR(200),
    description VARCHAR(300),
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Invoice linked to billing_payment
CREATE TABLE invoices (
    id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL REFERENCES billing_payments(id) ON DELETE CASCADE,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    pdf_url VARCHAR(500),
    hosted_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- System fee configuration per payment type
CREATE TABLE system_fees (
    id VARCHAR(36) PRIMARY KEY,
    payment_type VARCHAR(30) NOT NULL UNIQUE,
    fixed_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    percentage_fee DECIMAL(6,5) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Global billing configuration
CREATE TABLE billing_settings (
    id VARCHAR(36) PRIMARY KEY,
    default_currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    default_gateway VARCHAR(30) NOT NULL DEFAULT 'MOCK',
    webhook_secret VARCHAR(200),
    pix_expiration_minutes INT NOT NULL DEFAULT 30,
    invoice_expiration_days INT NOT NULL DEFAULT 7,
    default_trial_days INT NOT NULL DEFAULT 14,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Seed: features
INSERT INTO features (id, feature_key, name, description) VALUES
('feat-consultations',  'CONSULTATIONS',   'Consultas mensais',        'Número de consultas por mês'),
('feat-exams',          'EXAMS',            'Exames mensais',           'Número de exames por mês'),
('feat-procedures',     'PROCEDURES',       'Procedimentos mensais',    'Número de procedimentos por mês'),
('feat-patients',       'PATIENTS',         'Pacientes cadastrados',    'Número máximo de pacientes'),
('feat-users',          'USERS',            'Usuários',                 'Número de usuários na conta'),
('feat-employees',      'EMPLOYEES',        'Colaboradores',            'Número de colaboradores'),
('feat-storage',        'STORAGE',          'Armazenamento (GB)',       'Espaço de armazenamento em GB'),
('feat-reports',        'REPORTS',          'Relatórios avançados',     'Acesso a relatórios avançados'),
('feat-api',            'API',              'API de integração',        'Acesso à API REST'),
('feat-ai',             'AI',               'Inteligência Artificial',  'Funcionalidades de IA'),
('feat-whatsapp',       'WHATSAPP',         'Integração WhatsApp',      'Mensagens via WhatsApp'),
('feat-support',        'SUPPORT',          'Suporte',                  'Nível de suporte'),
('feat-custom-domain',  'CUSTOM_DOMAIN',    'Domínio personalizado',    'Domínio customizado');

-- Seed: system fees (fixed values saved at charge time)
INSERT INTO system_fees (id, payment_type, fixed_fee, percentage_fee, active) VALUES
('sysfee-consultation', 'CONSULTATION', 5.00,  0.00000, TRUE),
('sysfee-procedure',    'PROCEDURE',    10.00, 0.02000, TRUE),
('sysfee-exam',         'EXAM',         3.00,  0.00000, TRUE),
('sysfee-subscription', 'SUBSCRIPTION', 0.00,  0.00000, TRUE);

-- Seed: billing settings singleton
INSERT INTO billing_settings (id, default_currency, default_gateway, pix_expiration_minutes, invoice_expiration_days, default_trial_days)
VALUES ('billing-cfg-1', 'BRL', 'MOCK', 30, 7, 14);
