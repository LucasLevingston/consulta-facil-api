CREATE TABLE plans (
    id             VARCHAR(36)   PRIMARY KEY,
    slug           VARCHAR(50)   NOT NULL UNIQUE,
    name           VARCHAR(100)  NOT NULL,
    description    TEXT,
    tier           VARCHAR(30)   NOT NULL,
    billing_period VARCHAR(20)   NOT NULL,
    price          DECIMAL(10,2) NOT NULL,
    frequency      INTEGER       NOT NULL DEFAULT 1,
    frequency_type VARCHAR(20)   NOT NULL DEFAULT 'months',
    features       TEXT,
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    display_order  INTEGER       NOT NULL DEFAULT 0,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

INSERT INTO plans (id, slug, name, description, tier, billing_period, price, frequency, frequency_type, features, status, display_order) VALUES
('plan-basic-m',  'basic-monthly',       'Básico Mensal',         'Plano básico para profissionais iniciantes', 'BASIC',      'MONTHLY',    79.90,    1,  'months', 'Agenda online,Prontuário eletrônico,Até 50 pacientes',                                                                   'ACTIVE', 1),
('plan-basic-s',  'basic-semiannual',    'Básico Semestral',      'Básico com desconto semestral',              'BASIC',      'SEMIANNUAL', 399.00,   6,  'months', 'Agenda online,Prontuário eletrônico,Até 50 pacientes',                                                                   'ACTIVE', 2),
('plan-basic-a',  'basic-annual',        'Básico Anual',          'Básico com desconto anual',                  'BASIC',      'ANNUAL',     719.88,   12, 'months', 'Agenda online,Prontuário eletrônico,Até 50 pacientes',                                                                   'ACTIVE', 3),
('plan-pro-m',    'monthly',             'Pro Mensal',            'Plano profissional mensal completo',         'PRO',        'MONTHLY',    149.90,   1,  'months', 'Agenda online,Prontuário eletrônico,Pacientes ilimitados,Relatórios,Telemedicina',                                        'ACTIVE', 4),
('plan-pro-s',    'pro-semiannual',      'Pro Semestral',         'Pro com desconto semestral',                 'PRO',        'SEMIANNUAL', 749.40,   6,  'months', 'Agenda online,Prontuário eletrônico,Pacientes ilimitados,Relatórios,Telemedicina',                                        'ACTIVE', 5),
('plan-pro-a',    'yearly',              'Pro Anual',             'Pro com desconto anual',                     'PRO',        'ANNUAL',     1349.80,  12, 'months', 'Agenda online,Prontuário eletrônico,Pacientes ilimitados,Relatórios,Telemedicina',                                        'ACTIVE', 6),
('plan-clinic-m', 'clinic-monthly',      'Clínica Mensal',        'Plano para clínicas mensal',                 'CLINIC',     'MONTHLY',    700.00,   1,  'months', 'Múltiplos profissionais,Agenda compartilhada,Prontuário eletrônico,Relatórios avançados,Telemedicina,Suporte prioritário', 'ACTIVE', 7),
('plan-clinic-s', 'clinic-semiannual',   'Clínica Semestral',     'Clínica com desconto semestral',             'CLINIC',     'SEMIANNUAL', 3500.00,  6,  'months', 'Múltiplos profissionais,Agenda compartilhada,Prontuário eletrônico,Relatórios avançados,Telemedicina,Suporte prioritário', 'ACTIVE', 8),
('plan-clinic-a', 'clinic-yearly',       'Clínica Anual',         'Clínica com desconto anual',                 'CLINIC',     'ANNUAL',     6300.00,  12, 'months', 'Múltiplos profissionais,Agenda compartilhada,Prontuário eletrônico,Relatórios avançados,Telemedicina,Suporte prioritário', 'ACTIVE', 9),
('plan-ent-m',    'enterprise-monthly',  'Enterprise Mensal',     'Plano enterprise para grandes redes',        'ENTERPRISE', 'MONTHLY',    2000.00,  1,  'months', 'Tudo do Clínica,API de integração,White label,SLA garantido,Gerente de conta dedicado',                                  'ACTIVE', 10),
('plan-ent-s',    'enterprise-semiannual','Enterprise Semestral', 'Enterprise com desconto semestral',          'ENTERPRISE', 'SEMIANNUAL', 10000.00, 6,  'months', 'Tudo do Clínica,API de integração,White label,SLA garantido,Gerente de conta dedicado',                                  'ACTIVE', 11),
('plan-ent-a',    'enterprise-annual',   'Enterprise Anual',      'Enterprise com desconto anual',              'ENTERPRISE', 'ANNUAL',     18000.00, 12, 'months', 'Tudo do Clínica,API de integração,White label,SLA garantido,Gerente de conta dedicado',                                  'ACTIVE', 12);
