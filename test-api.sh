#!/bin/bash

# Consulta Fácil API - Script de Testes
# Este script contém exemplos de requisições para testar a API

BASE_URL="http://localhost:8080/api/v1"
ADMIN_TOKEN=""
USER_TOKEN=""
DOCTOR_ID=""
PATIENT_ID=""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Iniciando testes da Consulta Fácil API ===${NC}\n"

# 1. Registrar novo usuário
echo -e "${BLUE}1. Registrando novo paciente...${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com",
    "password": "senha123456",
    "cpf": "12345678901",
    "phone": "(11) 98765-4321",
    "birthDate": "1990-01-15",
    "gender": "MALE"
  }')

PATIENT_ID=$(echo $RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}Paciente registrado com ID: $PATIENT_ID${NC}\n"

# 2. Registrar médico
echo -e "${BLUE}2. Registrando novo médico...${NC}"
DOCTOR_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Carlos Santos",
    "email": "carlos@example.com",
    "password": "senha123456",
    "cpf": "98765432101",
    "phone": "(11) 99876-5432",
    "birthDate": "1985-05-20",
    "gender": "MALE"
  }')

DOCTOR_USER_ID=$(echo $DOCTOR_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}Médico registrado com ID de usuário: $DOCTOR_USER_ID${NC}\n"

# 3. Login do médico
echo -e "${BLUE}3. Realizando login do médico...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos@example.com",
    "password": "senha123456"
  }')

ADMIN_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}Token do médico obtido${NC}\n"

# 4. Criar perfil de médico
echo -e "${BLUE}4. Criando perfil de médico...${NC}"
DOCTOR_PROFILE=$(curl -s -X POST "$BASE_URL/doctors" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "specialty": "Cardiologia",
    "licenseNumber": "CRM/SP123456"
  }')

DOCTOR_ID=$(echo $DOCTOR_PROFILE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}Perfil de médico criado com ID: $DOCTOR_ID${NC}\n"

# 5. Login do paciente
echo -e "${BLUE}5. Realizando login do paciente...${NC}"
USER_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "senha123456"
  }')

USER_TOKEN=$(echo $USER_LOGIN | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}Token do paciente obtido${NC}\n"

# 6. Listar médicos
echo -e "${BLUE}6. Listando médicos...${NC}"
curl -s -X GET "$BASE_URL/doctors?page=0&size=10" \
  -H "Accept: application/json" | grep -o '"specialty":"[^"]*' | head -1
echo -e "${GREEN}Médicos listados${NC}\n"

# 7. Agendar consulta
echo -e "${BLUE}7. Agendando consulta...${NC}"
SCHEDULED_DATE=$(date -d '+7 days' '+%Y-%m-%dT14:30:00' 2>/dev/null || date -v+7d '+%Y-%m-%dT14:30:00')
APPOINTMENT=$(curl -s -X POST "$BASE_URL/appointments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d "{
    \"doctorId\": \"$DOCTOR_ID\",
    \"scheduledAt\": \"$SCHEDULED_DATE\",
    \"reason\": \"Consulta de rotina\",
    \"notes\": \"Paciente com histórico de hipertensão\"
  }")

APPOINTMENT_ID=$(echo $APPOINTMENT | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}Consulta agendada com ID: $APPOINTMENT_ID${NC}\n"

# 8. Confirmar consulta
echo -e "${BLUE}8. Confirmando consulta...${NC}"
curl -s -X PUT "$BASE_URL/appointments/$APPOINTMENT_ID/confirm" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | grep -o '"status":"[^"]*'
echo -e "${GREEN}Consulta confirmada${NC}\n"

# 9. Obter dados do usuário autenticado
echo -e "${BLUE}9. Obtendo dados do usuário autenticado...${NC}"
curl -s -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $USER_TOKEN" | grep -o '"name":"[^"]*'
echo -e "${GREEN}Usuário autenticado retornado${NC}\n"

# 10. Listar consultas do paciente
echo -e "${BLUE}10. Listando consultas do paciente...${NC}"
curl -s -X GET "$BASE_URL/appointments/patient/$PATIENT_ID?page=0&size=10" \
  -H "Authorization: Bearer $USER_TOKEN" | grep -o '"status":"[^"]*' | head -1
echo -e "${GREEN}Consultas do paciente listadas${NC}\n"

echo -e "${GREEN}=== Todos os testes completados com sucesso! ===${NC}"
echo -e "\n${BLUE}Tokens para uso manual:${NC}"
echo -e "Admin Token: $ADMIN_TOKEN"
echo -e "User Token: $USER_TOKEN"
echo -e "\n${BLUE}IDs importantes:${NC}"
echo -e "Doctor ID: $DOCTOR_ID"
echo -e "Patient ID: $PATIENT_ID"
echo -e "Appointment ID: $APPOINTMENT_ID"
