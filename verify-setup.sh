#!/bin/bash

# Script de verificação - Consulta Fácil API
# Verifica se tudo está configurado corretamente

echo "================================"
echo "🔍 Verificando Configuração"
echo "================================"
echo ""

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

ERRORS=0

# 1. Verificar Java
echo -n "1. Verificando Java... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✓${NC} $JAVA_VERSION"
else
    echo -e "${RED}✗ Java não encontrado${NC}"
    ERRORS=$((ERRORS+1))
fi

# 2. Verificar Docker
echo -n "2. Verificando Docker... "
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    echo -e "${GREEN}✓${NC} $DOCKER_VERSION"
else
    echo -e "${RED}✗ Docker não encontrado${NC}"
    ERRORS=$((ERRORS+1))
fi

# 3. Verificar Docker Compose
echo -n "3. Verificando Docker Compose... "
if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version)
    echo -e "${GREEN}✓${NC} $COMPOSE_VERSION"
else
    echo -e "${RED}✗ Docker Compose não encontrado${NC}"
    ERRORS=$((ERRORS+1))
fi

# 4. Verificar Gradle
echo -n "4. Verificando Gradle... "
if [ -f "gradlew" ] || [ -f "gradlew.bat" ]; then
    echo -e "${GREEN}✓${NC} Wrapper encontrado"
else
    echo -e "${RED}✗ Gradle wrapper não encontrado${NC}"
    ERRORS=$((ERRORS+1))
fi

# 5. Verificar estrutura de pastas
echo -n "5. Verificando estrutura de pastas... "
MISSING_DIRS=0
for dir in src/main/java/com/example/consulta/{api,application,core,domain} src/main/resources/db/migration; do
    if [ ! -d "$dir" ]; then
        echo "❌ Faltando: $dir"
        MISSING_DIRS=$((MISSING_DIRS+1))
    fi
done
if [ $MISSING_DIRS -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Todas as pastas existem"
else
    echo -e "${RED}✗ $MISSING_DIRS pastas faltando${NC}"
    ERRORS=$((ERRORS+1))
fi

# 6. Verificar arquivos importantes
echo -n "6. Verificando arquivos importantes... "
MISSING_FILES=0
for file in build.gradle compose.yaml application.properties src/main/resources/db/migration/V1__initial_schema.sql; do
    if [ ! -f "$file" ]; then
        echo "❌ Faltando: $file"
        MISSING_FILES=$((MISSING_FILES+1))
    fi
done
if [ $MISSING_FILES -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Todos os arquivos existem"
else
    echo -e "${RED}✗ $MISSING_FILES arquivos faltando${NC}"
    ERRORS=$((ERRORS+1))
fi

# 7. Verificar if Docker está rodando
echo -n "7. Verificando Docker daemon... "
if docker ps &> /dev/null; then
    echo -e "${GREEN}✓${NC} Docker está rodando"
else
    echo -e "${YELLOW}⚠${NC} Docker não está rodando (será iniciado ao executar compose)"
fi

echo ""
echo "================================"

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✓ Tudo está pronto!${NC}"
    echo ""
    echo "Próximos passos:"
    echo "1. Executar: docker-compose up -d"
    echo "2. Executar: ./gradlew bootRun"
    echo "3. Acessar: http://localhost:8080/api/v1/swagger-ui.html"
else
    echo -e "${RED}✗ $ERRORS erro(s) encontrado(s)${NC}"
    echo ""
    echo "Instale os pré-requisitos faltantes:"
    echo "- Java 26+: https://www.oracle.com/java/technologies/downloads/"
    echo "- Docker: https://www.docker.com/products/docker-desktop"
    echo "- Git: https://git-scm.com/"
fi

echo "================================"
