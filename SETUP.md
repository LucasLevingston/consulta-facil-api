# 🚀 Guia de Instalação e Execução - Consulta Fácil API

## ✅ Pré-requisitos

Antes de começar, verifique se você tem instalado:

- **Java 26+**: [Download Java](https://www.oracle.com/java/technologies/downloads/)
  ```bash
  java -version
  ```

- **Docker & Docker Compose**: [Download Docker Desktop](https://www.docker.com/products/docker-desktop)
  ```bash
  docker --version
  docker-compose --version
  ```

- **Git**: [Download Git](https://git-scm.com/)
  ```bash
  git --version
  ```

## 📦 Passo 1: Clonar e Entrar no Diretório

```bash
cd /d/projetos/consulta\ facil/api
```

Ou no Windows PowerShell:
```powershell
cd "d:\projetos\consulta facil\api"
```

## 🐳 Passo 2: Iniciar o PostgreSQL com Docker Compose

```bash
docker-compose up -d
```

**Verificar se está rodando:**
```bash
docker-compose ps
```

Você deve ver:
```
NAME                    STATUS
consulta_facil_db       Up (healthy)
```

Se precisar ver os logs:
```bash
docker-compose logs -f postgres
```

## 📥 Passo 3: Compilar o Projeto

```bash
./gradlew clean build
```

No Windows PowerShell:
```powershell
.\gradlew.bat clean build
```

**Tempo esperado**: 2-5 minutos na primeira execução

Se tiver erro de permissão no Linux/Mac:
```bash
chmod +x gradlew
```

## ▶️ Passo 4: Executar a Aplicação

```bash
./gradlew bootRun
```

No Windows PowerShell:
```powershell
.\gradlew.bat bootRun
```

**Você verá algo como:**
```
Started ConsultaFacilApplication in X seconds
```

## ✅ Passo 5: Verificar se Está Rodando

### Via Browser
Abra: http://localhost:8080/api/v1/swagger-ui.html

Você deve ver a documentação interativa da API

### Via Terminal
```bash
curl -s http://localhost:8080/api/v1/docs | head -20
```

## 🧪 Passo 6: Testar a API

### Opção A: Usando Swagger UI (Mais Fácil)
1. Acesse: http://localhost:8080/api/v1/swagger-ui.html
2. Clique em "Authorize" no topo
3. Teste os endpoints pelo navegador

### Opção B: Usando cURL (Terminal)

#### Registrar novo usuário
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com",
    "password": "senha123456",
    "cpf": "12345678901",
    "phone": "(11) 98765-4321"
  }'
```

#### Fazer login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "senha123456"
  }'
```

Guarde o `token` retornado!

#### Usar o token para acessar endpoints protegidos
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

### Opção C: Usando o Script de Teste (Automático)

No Linux/Mac:
```bash
chmod +x test-api.sh
./test-api.sh
```

## 📋 Endpoints Principais

| Método | Endpoint | Autenticação | Descrição |
|--------|----------|--------------|-----------|
| POST | `/auth/register` | ❌ | Registrar novo usuário |
| POST | `/auth/login` | ❌ | Fazer login |
| GET | `/users/me` | ✅ | Dados do usuário autenticado |
| GET | `/doctors` | ❌ | Listar médicos |
| POST | `/doctors` | ✅ Admin | Criar perfil de médico |
| POST | `/appointments` | ✅ User | Agendar consulta |
| GET | `/appointments/{id}` | ✅ | Obter consulta |

## 🛑 Parar a Aplicação

### Parar a aplicação Spring Boot
- Pressione `Ctrl + C` no terminal

### Parar o Docker PostgreSQL
```bash
docker-compose down
```

Para remover os dados também:
```bash
docker-compose down -v
```

## 🔄 Reiniciar

```bash
# Parar tudo
docker-compose down

# Remover volumes (opcional, para limpar dados)
docker-compose down -v

# Iniciar novamente
docker-compose up -d

# Executar a aplicação
./gradlew bootRun
```

## 🐛 Troubleshooting

### Porta 5432 já está em uso
```bash
# Ver qual processo está usando a porta
lsof -i :5432  # Linux/Mac
netstat -ano | findstr :5432  # Windows

# Matar o processo ou usar outra porta
docker-compose down
```

### Erro de permissão no gradle
```bash
chmod +x gradlew
```

### PostgreSQL não conecta
```bash
# Verificar status
docker-compose ps

# Ver logs
docker-compose logs postgres

# Reiniciar
docker-compose restart postgres
```

### Porta 8080 já está em uso
Modifique em `application.properties`:
```properties
server.port=8081
```

### Erro ao compilar
```bash
# Limpar cache
./gradlew clean

# Compilar novamente
./gradlew build
```

## 📊 Verificar Base de Dados

### Via Docker CLI
```bash
docker-compose exec postgres psql -U postgres -d consulta_facil_db -c "SELECT * FROM users;"
```

### Via DBeaver (GUI)
1. Instale [DBeaver](https://dbeaver.io/)
2. Crie nova conexão PostgreSQL
3. Host: localhost, Port: 5432, Username: postgres, Password: postgres, Database: consulta_facil_db

## 📝 Logs

Ver logs em tempo real:
```bash
./gradlew bootRun | grep -E "ERROR|INFO|WARN"
```

Ver arquivo de log (se configurado):
```bash
tail -f logs/application.log
```

## 🔐 Segurança em Produção

Antes de fazer deploy:

1. **Mudar JWT Secret:**
   ```properties
   jwt.secret=${JWT_SECRET}
   ```
   Defina a variável de ambiente JWT_SECRET

2. **Mudar senha do PostgreSQL:**
   Altere em `compose.yaml` ou use variáveis de ambiente

3. **Desabilitar Swagger em produção:**
   ```properties
   springdoc.swagger-ui.enabled=false
   ```

4. **Usar HTTPS** com certificado válido

## 📚 Documentação Adicional

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [JWT Documentation](https://tools.ietf.org/html/rfc7519)
- [OpenAPI/Swagger](https://swagger.io/specification/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 🆘 Suporte

Se encontrar problemas:

1. Verifique os logs da aplicação
2. Confira se o Docker está rodando
3. Teste a conectividade do banco de dados
4. Abra uma issue no repositório

---

**Tudo pronto! Você agora tem a API completa rodando localmente.** 🎉
