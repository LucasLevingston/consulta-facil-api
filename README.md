# Consulta Fácil — API

API RESTful para gerenciamento de consultas médicas. Autenticação JWT, multi-role, filas de espera, exames, pagamentos e consultas online.

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.0 |
| Spring Security + JWT | JJWT 0.12.5 |
| PostgreSQL | 16 |
| Flyway | 10 |
| AWS S3 | SDK v2 |
| MercadoPago | SDK 2.1.7 |
| Testcontainers | 1.19.7 |
| JaCoCo (cobertura) | 0.8.11 |

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
DB_USER=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=sua-chave-secreta-muito-longa

# AWS S3
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_S3_BUCKET=
AWS_REGION=us-east-1

# MercadoPago
MERCADOPAGO_ACCESS_TOKEN=
```

## Roles

| Role | Descrição |
|---|---|
| `PATIENT` | Paciente: agenda consultas, faz upload de exames, paga |
| `PROFESSIONAL` | Profissional de saúde: confirma, completa, solicita exames, gerencia fila |
| `RECEPTIONIST` | Faz check-in de pacientes via QR, visualiza fila do dia |
| `ADMIN` | Acesso total |

## Funcionalidades

### Autenticação
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/auth/register` | Público |
| POST | `/auth/login` | Público |

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

### Consultas
| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/appointments` | PATIENT |
| GET | `/appointments/{id}` | Autenticado |
| GET | `/appointments/patient/{userId}` | PATIENT |
| GET | `/appointments/professional/{id}` | PROFESSIONAL / ADMIN |
| PUT | `/appointments/{id}/confirm` | PROFESSIONAL / ADMIN |
| PUT | `/appointments/{id}/reschedule` | PATIENT / PROFESSIONAL / ADMIN |
| PUT | `/appointments/{id}/cancel` | Autenticado |
| PUT | `/appointments/{id}/complete` | PROFESSIONAL / ADMIN |
| POST | `/appointments/{id}/rate` | PATIENT |
| DELETE | `/appointments/{id}` | ADMIN |

### Anamnese & Prontuário
| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/appointments/{id}/anamnesis` | Autenticado |
| PUT | `/appointments/{id}/anamnesis` | PATIENT / PROFESSIONAL / ADMIN |
| GET | `/appointments/{id}/prontuario` | Autenticado |
| PUT | `/appointments/{id}/prontuario` | PROFESSIONAL / ADMIN |

### Fila de Espera / Check-in via QR
| Método | Endpoint | Acesso |
|---|---|---|
| GET | `/appointments/{id}/checkin-token` | PATIENT |
| POST | `/appointments/checkin?token=` | RECEPTIONIST / PROFESSIONAL / ADMIN |
| GET | `/appointments/queue` | PROFESSIONAL / RECEPTIONIST / ADMIN |
| PUT | `/appointments/{id}/call` | PROFESSIONAL / ADMIN |

Fluxo: `PENDING → CONFIRMED → CHECKED_IN → IN_PROGRESS → COMPLETED`

### Consulta Online (Modality)
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
| POST | `/payments/webhook` | Público (MP callback) |

Fluxo: `UNPAID → PENDING_PAYMENT → PAID`

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
| POST | `/clinics/{id}/invites/{professionalId}` | PROFESSIONAL / ADMIN |
| POST | `/clinics/{id}/receptionists` | PROFESSIONAL / ADMIN |
| DELETE | `/clinics/{id}/receptionists/{id}` | PROFESSIONAL / ADMIN |
| GET | `/clinics/{id}/receptionists` | PROFESSIONAL / ADMIN |
| GET | `/clinics/{id}/working-hours` | Público |
| PUT | `/clinics/{id}/working-hours` | PROFESSIONAL / ADMIN |

## Estrutura

```
src/main/java/com/example/consulta/
├── api/
│   ├── controller/        # Endpoints REST
│   └── dto/               # Request/Response DTOs
├── application/
│   └── service/           # Um arquivo por operação (ex: ConfirmAppointmentService)
├── core/
│   ├── config/            # SecurityConfiguration, CORS, Swagger
│   ├── exception/         # GlobalExceptionHandler
│   ├── seeder/            # DatabaseSeeder (perfil seed)
│   └── security/          # JWT filter, CustomUserDetails
└── domain/
    ├── entity/            # Entidades JPA
    ├── enums/             # AppointmentStatus, UserRole, etc.
    └── repository/        # Spring Data repositories
```

## Deploy com Docker

```bash
# Build da imagem
docker build -t consulta-facil-api .

# Stack completa (db + api + web) via docker-compose no projeto web
cd ../web && docker compose up -d
```

Ver variáveis de ambiente necessárias na seção acima.
