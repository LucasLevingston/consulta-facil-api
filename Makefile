.PHONY: help \
        api-run api-test api-build api-seed \
        web-run web-build web-lint \
        up down logs

# Default target
help:
	@echo ""
	@echo "  Consulta Fácil — comandos disponíveis"
	@echo "  (rodar a partir da pasta api/)"
	@echo ""
	@echo "  API (Spring Boot / Gradle)"
	@echo "    make api-run       Sobe a API localmente (porta 8080)"
	@echo "    make api-seed      Sobe a API com perfil seed (reset + dados fake)"
	@echo "    make api-test      Executa todos os testes"
	@echo "    make api-build     Gera o JAR de produção"
	@echo ""
	@echo "  Web (Next.js)"
	@echo "    make web-run       Sobe o frontend em modo dev (porta 3000)"
	@echo "    make web-build     Build de produção"
	@echo "    make web-lint      Lint com Biome"
	@echo ""
	@echo "  Docker / Infra"
	@echo "    make up            docker compose up -d (banco + dependências)"
	@echo "    make down          docker compose down"
	@echo "    make logs          docker compose logs -f"
	@echo ""

# ── API ──────────────────────────────────────────────────────────────────────

api-run:
	./gradlew bootRun

api-seed:
	./gradlew bootRun --args='--spring.profiles.active=seed'

api-test:
	./gradlew test

api-build:
	./gradlew bootJar

# ── Web ──────────────────────────────────────────────────────────────────────

web-run:
	cd ../web && npm run dev

web-build:
	cd ../web && npm run build

web-lint:
	cd ../web && npm run lint

# ── Docker ───────────────────────────────────────────────────────────────────

up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f
