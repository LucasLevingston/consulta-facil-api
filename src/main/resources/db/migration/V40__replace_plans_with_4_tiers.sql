-- Add max_appointments column (NULL = unlimited)
ALTER TABLE plans ADD COLUMN IF NOT EXISTS max_appointments INTEGER;

-- Remove existing 12 plans and replace with 4 tier-based plans
DELETE FROM plans;

INSERT INTO plans (id, slug, name, description, tier, billing_period, price, frequency, frequency_type, features, max_appointments, status, display_order) VALUES
(
    'plan-free',
    'free',
    'Gratuito',
    'Comece sem custo. Ideal para testar a plataforma.',
    'FREE',
    'MONTHLY',
    0.00,
    1,
    'months',
    'Agenda online,Perfil profissional,App mobile,Até 15 consultas por mês',
    15,
    'ACTIVE',
    1
),
(
    'plan-starter',
    'starter',
    'Starter',
    'Para profissionais em crescimento com mais consultas e prontuário eletrônico.',
    'STARTER',
    'MONTHLY',
    79.90,
    1,
    'months',
    'Tudo do Gratuito,Prontuário eletrônico,Confirmação automática,Relatórios básicos,Até 50 consultas por mês',
    50,
    'ACTIVE',
    2
),
(
    'plan-pro',
    'pro',
    'Pro',
    'Para profissionais consolidados com telemedicina, IA e suporte prioritário.',
    'PRO',
    'MONTHLY',
    149.90,
    1,
    'months',
    'Tudo do Starter,Telemedicina,Anamnese com IA,Agendamento por voz,Relatórios avançados,Suporte prioritário,Até 200 consultas por mês',
    200,
    'ACTIVE',
    3
),
(
    'plan-clinic',
    'clinic',
    'Clínica',
    'Para clínicas com múltiplos profissionais e volume ilimitado de consultas.',
    'CLINIC',
    'MONTHLY',
    399.90,
    1,
    'months',
    'Tudo do Pro,Múltiplos profissionais,Dashboard administrativo,API de integração,Gerente de conta dedicado,Consultas ilimitadas',
    NULL,
    'ACTIVE',
    4
);
