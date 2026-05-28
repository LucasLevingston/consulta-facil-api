# Consulta Fácil — API

API RESTful para gerenciamento de consultas médicas. Multi-role, filas de espera, pagamentos, notificações assíncronas, telemedicina e IA conversacional via WhatsApp.

## Stack

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Runtime |
| Spring Boot | 3.2.0 | Framework |
| Spring Security + JWT | JJWT 0.12.5 | Autenticação |
| PostgreSQL | 16 | Banco de dados |
| Flyway | 10 | Migrações |
| RabbitMQ (CloudAMQP) | 3.x | Mensageria assíncrona |
| Redis (ElastiCache) | 7.x | Cache de leitura |
| AWS S3 | SDK v2 | Upload de imagens/documentos |
| AWS SES | SDK v2 | E-mails transacionais |
| MercadoPago | SDK 2.1.7 | Pagamentos |
| Twilio | — | Notificações WhatsApp |
| Anthropic Claude | Haiku 4.5 | Agente IA via WhatsApp |
| Testcontainers | 1.19.7 | Testes de integração |
| JaCoCo | 0.8.11 | Cobertura de testes |

## Pré-requisitos

- Java 17+
- Docker e Docker Compose
- (Opcional) Make

## Setup rápido

```bash
# 1. Sobe o banco
make up          # ou: docker compose up -d

# 2. Roda com dados de teste
make seed        # ou: ./gradlew bootRun --args='--spring.profiles.active=seed'
```

API disponível em `http://localhost:8080/v1`
Swagger UI: `http://localhost:8080/v1/swagger-ui.html`

## Comandos

```bash
make run         # dev sem seed
make seed        # dev com dados fake
make test        # testes
make coverage    # testes + relatório JaCoCo
make build       # JAR de produção
make openapi     # gera openapi.json e copia para ../web
make up / down   # docker compose
```

## Variáveis de Ambiente

```env
# Banco
DB_URL=jdbc:postgresql://localhost:5432/consulta_facil_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=sua-chave-secreta-min-32-chars

# Cache
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_URL=amqp://guest:guest@localhost:5672

# AWS
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_REGION=us-east-1
AWS_S3_BUCKET=consulta-facil-images
AWS_SES_FROM_EMAIL=noreply@consulta-facil.com

# MercadoPago
MERCADOPAGO_ACCESS_TOKEN=
MERCADOPAGO_WEBHOOK_SECRET=

# Twilio WhatsApp
TWILIO_ACCOUNT_SID=
TWILIO_AUTH_TOKEN=
TWILIO_WHATSAPP_FROM=whatsapp:+14155238886

# Anthropic AI
ANTHROPIC_API_KEY=

# URLs
APP_URL=http://localhost:3000
```

## Roles

| Role | Descrição |
|---|---|
| `PATIENT` | Agenda consultas, faz upload de exames, paga |
| `PROFESSIONAL` | Confirma, completa, preenche prontuário, gerencia fila e serviços |
| `RECEPTIONIST` | Faz check-in de pacientes via QR, visualiza fila |
| `ADMIN` | Acesso total |

## Endpoints

### Autenticação
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/auth/register` | Público |
| POST | `/auth/login` | Público |
| POST | `/auth/forgot-password` | Público |
| POST | `/auth/reset-password` | Público |

### Usuários
| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/users/me` | Autenticado |
| GET | `/users/{id}` | Autenticado |
| DELETE | `/users/{id}` | ADMIN |

### Profissionais
| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/professionals` | Público |
| GET | `/professionals/search?specialty=` | Público |
| GET | `/professionals/nearby?lat=&lng=&radiusKm=` | Público |
| GET | `/professionals/{id}` | Público |
| POST | `/professionals` | PATIENT / ADMIN |
| PUT | `/professionals/{id}` | PROFESSIONAL / ADMIN |
| DELETE | `/professionals/{id}` | ADMIN |
| GET | `/professionals/applications` | ADMIN |
| PUT | `/professionals/{id}/approve` | ADMIN |
| PUT | `/professionals/{id}/reject` | ADMIN |
| GET | `/professionals/me/schedule` | PROFESSIONAL / ADMIN |
| PUT | `/professionals/me/schedule` | PROFESSIONAL / ADMIN |
| PUT | `/professionals/me/consultation-price` | PROFESSIONAL |
| PUT | `/professionals/me/payment-settings` | PROFESSIONAL |

### Consultas
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/appointments` | PATIENT |
| GET | `/appointments/{id}` | PATIENT / PROFESSIONAL (próprias) |
| GET | `/appointments/patient/{userId}` | PATIENT (próprias) / ADMIN |
| GET | `/appointments/professional/{id}` | PROFESSIONAL / ADMIN |
| PUT | `/appointments/{id}/confirm` | PROFESSIONAL (própria consulta) |
| PUT | `/appointments/{id}/reschedule` | PATIENT / PROFESSIONAL (própria) |
| PUT | `/appointments/{id}/cancel` | PATIENT / PROFESSIONAL (própria) |
| PUT | `/appointments/{id}/complete` | PROFESSIONAL (própria consulta) |
| POST | `/appointments/{id}/rate` | PATIENT (própria) |
| DELETE | `/appointments/{id}` | ADMIN |

### Anamnese & Prontuário Clínico
| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/appointments/{id}/anamnesis` | PATIENT / PROFESSIONAL (própria consulta) |
| PUT | `/appointments/{id}/anamnesis` | PATIENT / PROFESSIONAL (própria) |
| GET | `/appointments/{id}/clinicalNote` | PATIENT / PROFESSIONAL (própria consulta) |
| PUT | `/appointments/{id}/clinicalNote` | PROFESSIONAL (própria consulta) |

### Fila de Espera / Check-in via QR
| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/appointments/{id}/checkin-token` | PATIENT |
| POST | `/appointments/checkin?token=` | RECEPTIONIST / PROFESSIONAL / ADMIN |
| GET | `/appointments/queue` | PROFESSIONAL / RECEPTIONIST / ADMIN |
| PUT | `/appointments/{id}/call` | PROFESSIONAL / ADMIN |

Fluxo: `PENDING → CONFIRMED → CHECKED_IN → IN_PROGRESS → COMPLETED`

### Telemedicina
| Método | Endpoint | Acesso |
|---|---|---|
| PUT | `/appointments/{id}/modality` | PROFESSIONAL / ADMIN |
| POST | `/appointments/{id}/meet-link` | PROFESSIONAL / ADMIN |

### Exames
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/appointments/{id}/exams` | PROFESSIONAL / ADMIN |
| GET | `/appointments/{id}/exams` | PATIENT / PROFESSIONAL / ADMIN |
| PUT | `/exams/{id}/upload` | PATIENT (multipart) |
| PUT | `/exams/{id}/review` | PROFESSIONAL / ADMIN |

### Pagamentos (MercadoPago)
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/appointments/{id}/payment` | PATIENT |
| POST | `/payments/webhook` | Público — validado via HMAC |

Fluxo: `UNPAID → PENDING_PAYMENT → PAID`

### Serviços do Profissional
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/professional-services` | PROFESSIONAL |
| GET | `/professional-services/{professionalId}` | Público |
| PUT | `/professional-services/{id}` | PROFESSIONAL (próprio) |
| DELETE | `/professional-services/{id}` | PROFESSIONAL (próprio) |

### Solicitações de Procedimento
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/procedure-requests` | PROFESSIONAL |
| GET | `/procedure-requests/mine` | PATIENT / PROFESSIONAL |
| POST | `/procedure-requests/{id}/schedule` | PATIENT |
| PUT | `/procedure-requests/{id}/cancel` | PATIENT / PROFESSIONAL |

### Clínicas
| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/clinics` | Público |
| GET | `/clinics/nearby?lat=&lng=` | Público |
| GET | `/clinics/{id}` | Público |
| POST | `/clinics` | PROFESSIONAL / ADMIN |
| PUT | `/clinics/{id}` | PROFESSIONAL / ADMIN |
| GET | `/clinics/my` | PROFESSIONAL / ADMIN |
| POST | `/clinics/{id}/members/{professionalId}` | PROFESSIONAL / ADMIN |
| DELETE | `/clinics/{id}/members/{professionalId}` | PROFESSIONAL / ADMIN |
| POST | `/clinics/{id}/receptionists` | PROFESSIONAL / ADMIN |
| DELETE | `/clinics/{id}/receptionists/{id}` | PROFESSIONAL / ADMIN |
| GET | `/clinics/{id}/receptionists` | PROFESSIONAL / ADMIN |
| GET | `/clinics/{id}/working-hours` | Público |
| PUT | `/clinics/{id}/working-hours` | PROFESSIONAL / ADMIN |

### Assinaturas
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/subscriptions/checkout` | PROFESSIONAL |
| POST | `/subscriptions/webhook` | Público |

### WhatsApp
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/whatsapp/webhook` | Público (Twilio callback) |

Agente IA (Claude Haiku) responde via WhatsApp: busca de profissionais, agendamento, consulta de status e cancelamento de consultas.

## Mensageria Assíncrona (RabbitMQ)

Todos os side-effects de notificação são desacoplados do request HTTP via exchange `consulta-facil` (topic):

| Routing Key | Consumidor |
|---|---|
| `appointments.created` | E-mail + WhatsApp |
| `appointments.canceled` | E-mail + WhatsApp |
| `appointments.confirmed` | E-mail + WhatsApp |
| `payments.succeeded` | E-mail |
| `payments.failed` | E-mail |

Dead-letter queue com retry exponencial (1s → 5s → 25s, 3 tentativas).

## Cache (Redis)

| Cache | TTL | Invalidação |
|---|---|---|
| `professional-services` | 5 min | create / update / deactivate |
| `professional-profile` | 5 min | update / delete |
| `professional-schedule` | 10 min | save schedule |

## Segurança

- Ownership validation: paciente/profissional só acessa **seus próprios** recursos
- JWT com expiração de 24h
- Webhook MercadoPago validado via HMAC-SHA256 + replay prevention (±5 min)
- Secrets via AWS SSM Parameter Store (nunca em application.properties)

## Estrutura

```
src/main/java/com/example/consulta/
├── api/
│   ├── controller/        # Endpoints REST
│   └── dto/               # Request/Response DTOs
├── application/
│   ├── consumer/          # RabbitMQ consumers (email, WhatsApp)
│   ├── scheduler/         # Agendadores (lembretes de consulta)
│   └── service/           # Um arquivo por operação
├── core/
│   ├── config/            # Security, CORS, Redis, MercadoPago, SES
│   ├── exception/         # GlobalExceptionHandler
│   ├── messaging/         # RabbitMQConfig, EventPublisher, events
│   ├── seeder/            # DatabaseSeeder (perfil seed)
│   └── security/          # JWT filter, OwnershipValidator
└── domain/
    ├── entity/            # Entidades JPA
    ├── enums/             # AppointmentStatus, UserRole, etc.
    └── repository/        # Spring Data repositories
```

## Infraestrutura (AWS)

Deploy automatizado via Terraform + GitHub Actions. Ver `infra/`.

| Serviço | Uso |
|---|---|
| ECS Fargate | Containers da API e do frontend |
| RDS PostgreSQL | Banco de dados (db.t3.micro) |
| ElastiCache Redis | Cache (opcional, `enable_elasticache`) |
| ALB | Load balancer com HTTPS |
| Route53 + ACM | Domínio customizado + certificado |
| CloudFront | CDN (opcional, `enable_cloudfront`) |
| S3 | Armazenamento de imagens |
| SES | E-mails transacionais |
| SSM Parameter Store | Secrets |
| CloudWatch | Logs e métricas |

## Deploy com Docker

```bash
docker build -t consulta-facil-api .
cd ../web && docker compose up -d
```
