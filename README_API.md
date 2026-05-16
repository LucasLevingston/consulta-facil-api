# 🏥 Consulta Fácil - API de Agendamento de Consultas Médicas

API RESTful completa em Spring Boot para gerenciamento de agendamento de consultas médicas com autenticação JWT, roles de usuário e segurança.

## 📋 Índice

- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Como Executar](#-como-executar)
- [Documentação da API](#-documentação-da-api)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Endpoints](#-endpoints)
- [Autenticação](#-autenticação)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)

## 🚀 Tecnologias

### Backend
- **Java 26** - Linguagem de programação
- **Spring Boot 4.0.6** - Framework web
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - ORM e persistência
- **JWT (JJWT)** - Token de autenticação
- **PostgreSQL 16** - Banco de dados
- **Flyway** - Migrações de banco de dados
- **Lombok** - Redução de boilerplate
- **MapStruct** - Mapeamento de DTOs
- **Swagger/OpenAPI 3.0** - Documentação da API
- **Testcontainers** - Testes integrados

### Padrões e Arquitetura
- **Clean Architecture** - Separação de responsabilidades
- **SOLID Principles** - Princípios de design
- **Design Patterns** - Factory, Builder, Singleton, Repository
- **DTO Pattern** - Transferência de dados entre camadas
- **Service Layer** - Lógica de negócio centralizada

## 📦 Pré-requisitos

- Java 26+
- Docker e Docker Compose
- Git
- IDE (VS Code, IntelliJ IDEA, Eclipse)

## 🔧 Instalação

### 1. Clonar o repositório
```bash
cd /d/projetos/consulta\ facil/api
```

### 2. Compilar o projeto
```bash
./gradlew build
```

## ▶️ Como Executar

### Opção 1: Com Docker Compose (Recomendado)

```bash
# Iniciar o PostgreSQL
docker-compose up -d

# Compilar e executar a aplicação
./gradlew bootRun
```

A aplicação estará disponível em: `http://localhost:8080/api/v1`

### Opção 2: Localmente (sem Docker)

Você precisa ter PostgreSQL instalado localmente. Configure as credenciais no `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/consulta_facil_db
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

Depois execute:
```bash
./gradlew bootRun
```

### Parar os containers
```bash
docker-compose down
```

## 📚 Documentação da API

Acesse a documentação interativa (Swagger UI):

```
http://localhost:8080/api/v1/swagger-ui.html
```

Ou a documentação JSON:
```
http://localhost:8080/api/v1/docs
```

## 📁 Estrutura do Projeto

```
src/main/
├── java/com/example/consulta/
│   ├── api/
│   │   ├── controller/       # Controllers REST
│   │   └── dto/              # Data Transfer Objects
│   ├── application/
│   │   └── service/          # Lógica de negócio
│   ├── core/
│   │   ├── config/           # Configurações
│   │   ├── exception/        # Tratamento de exceções
│   │   └── security/         # JWT e segurança
│   └── domain/
│       ├── entity/           # Entidades JPA
│       ├── enums/            # Enums
│       └── repository/       # Repositórios
└── resources/
    ├── application.properties # Configurações
    └── db/migration/         # Scripts SQL Flyway
```

## 🔐 Endpoints

### 🔓 Autenticação (Público)

#### 1. Registrar Novo Usuário
```http
POST /auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123456",
  "cpf": "12345678901",
  "phone": "(11) 98765-4321",
  "birthDate": "1990-01-15",
  "gender": "MALE"
}
```

**Response (201 Created):**
```json
{
  "id": "uuid-string",
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER",
  "phone": "(11) 98765-4321",
  "cpf": "12345678901",
  "birthDate": "1990-01-15",
  "gender": "MALE",
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:30:00"
}
```

#### 2. Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "password": "senha123456"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "userId": "uuid-string",
  "email": "joao@email.com",
  "role": "USER"
}
```

### 👥 Usuários (Autenticado)

#### Obter Usuário Autenticado
```http
GET /users/me
Authorization: Bearer {token}
```

#### Obter Usuário por ID
```http
GET /users/{userId}
Authorization: Bearer {token}
```

#### Deletar Usuário (Admin)
```http
DELETE /users/{userId}
Authorization: Bearer {token}
```

### 👨‍⚕️ Médicos (Público/Autenticado)

#### Listar Todos os Médicos
```http
GET /doctors?page=0&size=10
```

#### Buscar Médicos por Especialidade
```http
GET /doctors/search?specialty=Cardiologia&page=0&size=10
```

#### Obter Médico por ID
```http
GET /doctors/{doctorId}
```

#### Criar Perfil de Médico (Admin)
```http
POST /doctors
Authorization: Bearer {token}
Content-Type: application/json

{
  "specialty": "Cardiologia",
  "licenseNumber": "CRM/SP12345"
}
```

#### Atualizar Perfil de Médico (Admin)
```http
PUT /doctors/{doctorId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "specialty": "Cardiologia",
  "licenseNumber": "CRM/SP12345"
}
```

#### Deletar Perfil de Médico (Admin)
```http
DELETE /doctors/{doctorId}
Authorization: Bearer {token}
```

### 📅 Consultas (Autenticado)

#### Agendar Consulta (Paciente)
```http
POST /appointments
Authorization: Bearer {token}
Content-Type: application/json

{
  "doctorId": "uuid-string",
  "scheduledAt": "2026-06-15T14:30:00",
  "reason": "Consulta de rotina",
  "notes": "Paciente com histórico de hipertensão"
}
```

#### Obter Consulta por ID
```http
GET /appointments/{appointmentId}
Authorization: Bearer {token}
```

#### Listar Consultas do Paciente
```http
GET /appointments/patient/{patientId}?page=0&size=10
Authorization: Bearer {token}
```

#### Listar Consultas do Médico (Admin)
```http
GET /appointments/doctor/{doctorId}?page=0&size=10
Authorization: Bearer {token}
```

#### Confirmar Consulta (Admin)
```http
PUT /appointments/{appointmentId}/confirm
Authorization: Bearer {token}
```

#### Cancelar Consulta
```http
PUT /appointments/{appointmentId}/cancel
Authorization: Bearer {token}
Content-Type: application/json

{
  "cancellationReason": "Motivo do cancelamento"
}
```

#### Marcar Consulta como Concluída (Admin)
```http
PUT /appointments/{appointmentId}/complete
Authorization: Bearer {token}
```

#### Deletar Consulta (Admin)
```http
DELETE /appointments/{appointmentId}
Authorization: Bearer {token}
```

## 🔐 Autenticação

A API usa **JWT (JSON Web Token)** para autenticação:

1. Faça login ou registre-se para obter um token
2. Inclua o token em todas as requisições subsequentes no header:
   ```
   Authorization: Bearer {seu_token_jwt}
   ```
3. O token expira em 24 horas (configurável)

## 🔒 Roles e Permissões

| Role | Permissões |
|------|-----------|
| **USER** | Agendar consultas, visualizar próprias consultas |
| **ADMIN** | Gerenciar médicos, confirmar/cancelar consultas, gerenciar usuários |

## 📝 Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```bash
# JWT
JWT_SECRET=sua-chave-secreta-muito-segura-mude-em-producao
JWT_EXPIRATION=86400000

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=consulta_facil_db
DB_USER=postgres
DB_PASSWORD=postgres

# Logging
LOGGING_LEVEL=INFO
```

Depois atualize o `application.properties`:

```properties
jwt.secret=${JWT_SECRET:seu-valor-padrao}
jwt.expiration=${JWT_EXPIRATION:86400000}
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:consulta_facil_db}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
```

## 🧪 Testes

Execute os testes com:

```bash
./gradlew test
```

Com cobertura de testes:

```bash
./gradlew test jacocoTestReport
```

## 🛠️ Scripts Úteis

### Limpar e compilar
```bash
./gradlew clean build
```

### Executar testes
```bash
./gradlew test
```

### Gerar documentação
```bash
./gradlew javadoc
```

### Verificar estilo de código
```bash
./gradlew checkstyleMain
```

## 📊 Modelo de Dados

```
┌─────────────────────────────────────────────┐
│               USERS                         │
│  (id, email, name, password, role, cpf)    │
└────────────┬────────────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼──────────────┐ ┌┴──────────────────┐
│PATIENT_PROFILES │ │DOCTOR_PROFILES    │
│                 │ │(specialty)        │
└────┬────────────┘ └──────────────────┘
     │
     ├─ APPOINTMENTS (patient <-> doctor)
     ├─ EMERGENCY_CONTACTS
     └─ MEDICAL_RECORDS
```

## 🤝 Contribuindo

1. Faça fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Distribuído sob a licença MIT. Veja `LICENSE` para mais informações.

## 📞 Suporte

Para dúvidas ou problemas, abra uma issue no repositório ou entre em contato em `support@consultafacil.com`.

---

**Desenvolvido com ❤️ por Consulta Fácil Team**
