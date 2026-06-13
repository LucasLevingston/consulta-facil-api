-- Insert order: (plan_id, feature_id, feature_value)
-- Positional insert works whether column is named 'value' (old V43) or 'feature_value' (new V43)

-- plan-free: limited access
INSERT INTO plan_features VALUES
('plan-free', 'feat-consultations', '10'),
('plan-free', 'feat-exams',         '5'),
('plan-free', 'feat-procedures',    '0'),
('plan-free', 'feat-patients',      '50'),
('plan-free', 'feat-users',         '1'),
('plan-free', 'feat-employees',     '0'),
('plan-free', 'feat-storage',       '1'),
('plan-free', 'feat-reports',       'false'),
('plan-free', 'feat-api',           'false'),
('plan-free', 'feat-ai',            'false'),
('plan-free', 'feat-whatsapp',      'false'),
('plan-free', 'feat-support',       'email'),
('plan-free', 'feat-custom-domain', 'false');

-- plan-starter: moderate limits
INSERT INTO plan_features VALUES
('plan-starter', 'feat-consultations', '100'),
('plan-starter', 'feat-exams',         '50'),
('plan-starter', 'feat-procedures',    '20'),
('plan-starter', 'feat-patients',      '500'),
('plan-starter', 'feat-users',         '3'),
('plan-starter', 'feat-employees',     '5'),
('plan-starter', 'feat-storage',       '10'),
('plan-starter', 'feat-reports',       'basic'),
('plan-starter', 'feat-api',           'false'),
('plan-starter', 'feat-ai',            'false'),
('plan-starter', 'feat-whatsapp',      'false'),
('plan-starter', 'feat-support',       'email'),
('plan-starter', 'feat-custom-domain', 'false');

-- plan-pro: high limits
INSERT INTO plan_features VALUES
('plan-pro', 'feat-consultations', 'unlimited'),
('plan-pro', 'feat-exams',         'unlimited'),
('plan-pro', 'feat-procedures',    'unlimited'),
('plan-pro', 'feat-patients',      'unlimited'),
('plan-pro', 'feat-users',         '10'),
('plan-pro', 'feat-employees',     '20'),
('plan-pro', 'feat-storage',       '50'),
('plan-pro', 'feat-reports',       'advanced'),
('plan-pro', 'feat-api',           'true'),
('plan-pro', 'feat-ai',            'true'),
('plan-pro', 'feat-whatsapp',      'true'),
('plan-pro', 'feat-support',       'priority'),
('plan-pro', 'feat-custom-domain', 'true');

-- plan-clinic: enterprise
INSERT INTO plan_features VALUES
('plan-clinic', 'feat-consultations', 'unlimited'),
('plan-clinic', 'feat-exams',         'unlimited'),
('plan-clinic', 'feat-procedures',    'unlimited'),
('plan-clinic', 'feat-patients',      'unlimited'),
('plan-clinic', 'feat-users',         'unlimited'),
('plan-clinic', 'feat-employees',     'unlimited'),
('plan-clinic', 'feat-storage',       '200'),
('plan-clinic', 'feat-reports',       'advanced'),
('plan-clinic', 'feat-api',           'true'),
('plan-clinic', 'feat-ai',            'true'),
('plan-clinic', 'feat-whatsapp',      'true'),
('plan-clinic', 'feat-support',       'dedicated'),
('plan-clinic', 'feat-custom-domain', 'true');
