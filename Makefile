.PHONY: help run seed test build clean coverage openapi up down logs

help:
	@echo ""
	@echo "  Consulta Fácil — API (Spring Boot 3 / Java 17 / Gradle)"
	@echo ""
	@echo "  Desenvolvimento"
	@echo "    make run         Sobe a API localmente (porta 8080)"
	@echo "    make seed        Sobe a API com dados de teste (perfil seed)"
	@echo "    make build       Gera o JAR de produção"
	@echo "    make clean       Remove arquivos de build"
	@echo ""
	@echo "  Testes"
	@echo "    make test        Executa todos os testes"
	@echo "    make coverage    Gera relatório de cobertura (build/reports/jacoco)"
	@echo ""
	@echo "  Documentação"
	@echo "    make openapi     Gera openapi.json e copia para ../web"
	@echo ""
	@echo "  Docker / Infra"
	@echo "    make up          docker compose up -d (banco PostgreSQL)"
	@echo "    make down        docker compose down"
	@echo "    make logs        docker compose logs -f"
	@echo ""

# ── Desenvolvimento ───────────────────────────────────────────────────────────

run:
	./gradlew bootRun

seed:
	./gradlew bootRun --args='--spring.profiles.active=seed'

build:
	./gradlew bootJar

clean:
	./gradlew clean

# ── Testes ────────────────────────────────────────────────────────────────────

test:
	./gradlew test

coverage:
	./gradlew test jacocoTestReport
	@echo "Relatório gerado em: build/reports/jacoco/test/html/index.html"

# ── Documentação ──────────────────────────────────────────────────────────────

openapi:
	./gradlew generateOpenApiDocs
	@echo "openapi.json copiado para ../web"

# ── Docker ────────────────────────────────────────────────────────────────────

up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f
