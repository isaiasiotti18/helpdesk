# HelpDesk SaaS

Sistema de gestão de chamados com chat realtime, filas inteligentes, SLA automatizado e métricas operacionais. Monolito modular com Java 21, Spring Boot 4, React 19 e PostgreSQL 16.

## Stack

**Backend**
- Java 21 · Spring Boot 4.0.5 · Spring Security (JWT) · Spring WebSocket (STOMP) · Spring Data JPA · Spring Boot Actuator
- Flyway 10 · HikariCP
- JJWT 0.12 · Lombok

**Frontend**
- React 19 · TypeScript 5.9 · Vite 8
- Tailwind CSS 4 · shadcn/ui · Sonner (toasts)
- Zustand · TanStack React Query 5 · React Hook Form + Zod
- React Router 7 · Axios · STOMP.js + SockJS

**Banco**
- PostgreSQL 16

**Infra**
- Docker · Docker Compose · Nginx · GitHub Actions CI

## Funcionalidades

### Core
- **Tickets** — criação, atribuição e ciclo de vida com máquina de estados (OPEN → IN_QUEUE → IN_PROGRESS → RESOLVED → CLOSED)
- **Chat realtime** — WebSocket (STOMP + SockJS) com persistência assíncrona em batch
- **Filas** — auto‑assign via algoritmo Least Active Agent
- **Categorias e departamentos** — roteamento automático pela categoria vinculada à fila
- **Notas internas** — canal WebSocket separado, visível só para agentes
- **Transferência** — transferir ticket para outro agente ou fila
- **Respostas prontas** — picker acionado com `/` no chat
- **Timeline de atividades** — histórico de ações por ticket

### Avançado
- **SLA** — políticas por prioridade, deadlines automáticos, verificação de breach a cada 60s
- **Notificações** — push via WebSocket + persistência + badge de não lidas
- **Dashboard de métricas** — KPIs (tempo médio de resposta, SLA compliance, carga por agente, tickets/dia)
- **CSAT** — avaliação 1–5 estrelas após fechamento
- **Busca full‑text** — título e descrição com debounce e dropdown de resultados

### Segurança
- JWT access token (1h) + refresh token (7d) com rotação
- Refresh tokens hasheados (SHA‑256) no banco, com revogação no logout
- Rate limiting no login (5 tentativas / 5 min por e‑mail)
- Validação de ownership (CLIENT só vê seus tickets)
- CORS configurável por ambiente
- Soft delete em tickets e usuários
- Validação do JWT secret (≥ 32 chars) no startup

### Arquitetura
- Modular Monolith com DDD‑lite (domain / application / controllers por módulo)
- Optimistic locking (`@Version`) em tickets
- JPA Specifications para filtros composáveis
- Paginação cursor‑based nas mensagens
- Batch insert de mensagens (buffer + flush a cada 200 ms)
- Audit log assíncrono via domain events
- HikariCP afinado para Neon

### Frontend
- Error boundaries (global + por rota)
- Refresh token interceptor com fila de requests
- Optimistic updates no chat
- Skeletons e empty states contextualizados
- Responsive (sidebar com Sheet no mobile)
- Datas relativas em pt‑BR (date‑fns)

## Pré‑requisitos

| Ferramenta | Versão | Quando precisa |
|---|---|---|
| Docker Engine + Compose v2 | recente | modo Docker (recomendado) |
| Java 21 (Temurin) | 21 | modo dev sem Docker |
| Node.js | ≥ 20 | modo dev sem Docker |
| `jq` | qualquer | opcional, para smoke tests |

## Como subir o projeto

Há dois modos. Escolha um.

### Modo A — Docker Compose (produção local, recomendado)

Sobe `db` + `backend` + `frontend` (nginx) em três containers.

```bash
git clone https://github.com/seu-usuario/helpdesk.git
cd helpdesk

# 1) Arquivo de ambiente
cp .env.example .env
# Edite .env e troque JWT_SECRET por algo com >= 32 caracteres
#   ex: JWT_SECRET=$(openssl rand -base64 48)

# 2) Build e subida
docker compose up -d --build

# 3) Aguardar os containers ficarem "healthy"
docker compose ps
docker compose logs -f backend   # Ctrl+C quando vir "Started BackendApplication"
```

Endpoints no host:
- Frontend (nginx): <http://localhost>
- Backend (API direta): <http://localhost:8080>
- Health do Actuator: <http://localhost:8080/actuator/health> → `{"status":"UP"}`

**Pós‑boot — criar o primeiro ADMIN**

Novos usuários são criados com role `CLIENT`. Para promover um usuário a `ADMIN`:

```bash
# 1) Registre o usuário pela UI (http://localhost) ou via API:
curl -s -X POST http://localhost/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Admin","email":"admin@example.com","password":"Senha123!"}'

# 2) Promova no banco:
docker compose exec db psql -U helpdesk -c \
  "UPDATE users SET role='ADMIN' WHERE email='admin@example.com';"
```

Comandos úteis do stack:

```bash
docker compose ps                   # status e health
docker compose logs backend         # logs do Spring
docker compose logs frontend        # logs do nginx
docker compose down                 # derruba preservando volume
docker compose down -v              # derruba APAGANDO o volume do pg
```

### Modo B — Desenvolvimento (hot reload)

Sobe só o Postgres em container; backend e frontend rodam no host.

```bash
# 1) Postgres em Docker
docker compose -f docker-compose.dev.yml up -d

# 2) Configurar o backend (criar a partir do template)
cp backend/src/main/resources/application-dev.properties.example \
   backend/src/main/resources/application-dev.properties
# Edite o arquivo: ajustar url/usuário/senha do banco e trocar app.jwt.secret

# 3) Backend (terminal 1)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 4) Frontend (terminal 2)
cd frontend
npm install
npm run dev
```

Acesse <http://localhost:5173>. O Vite faz proxy de `/api` e `/ws` para `localhost:8080`.

> O arquivo `application-dev.properties` está no `.gitignore` — não comite credenciais.

## Variáveis de ambiente

### Do host (arquivo `.env`, modo Docker)
| Variável | Descrição | Default |
|---|---|---|
| `JWT_SECRET` | Segredo do JWT (≥ 32 chars) | **obrigatório** |

### Injetadas nos containers (modo Docker)
Definidas em `docker-compose.yml`. Normalmente não precisa mexer.

| Variável | Descrição | Valor |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil do Spring | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC URL | `jdbc:postgresql://db:5432/helpdesk` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do pg | `helpdesk` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do pg | `helpdesk` |
| `SPRING_FLYWAY_ENABLED` | Migrations no boot | `true` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Política de DDL | `validate` |
| `APP_JWT_SECRET` | Secret do JWT | `${JWT_SECRET}` |
| `APP_CORS_ALLOWED_ORIGINS` | Origens permitidas | `http://localhost,http://localhost:80` |

Spring aplica *relaxed binding*: `SPRING_DATASOURCE_URL` sobrescreve `spring.datasource.url`, `APP_JWT_SECRET` vira `app.jwt.secret`, etc.

## Estrutura do projeto

```
helpdesk/
├── backend/
│   ├── src/main/java/com/helpdesk/backend/
│   │   ├── config/                  # Security, WebSocket, Async, CORS
│   │   ├── shared/                  # Exceptions, DTOs, JWT, Audit
│   │   └── modules/
│   │       ├── auth/                # Login, registro, refresh token
│   │       ├── user/                # Entidade User, roles
│   │       ├── ticket/              # Tickets, SLA, Timeline, CSAT, Busca, Rating, Transfer
│   │       ├── queue/               # Filas, AgentStatus, assignment
│   │       ├── chat/                # Sessões, mensagens, notas, respostas prontas
│   │       ├── category/            # Categorias e departamentos
│   │       ├── notification/        # Notificações realtime
│   │       └── metrics/             # Dashboard de métricas
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-dev.properties.example
│   │   ├── application-prod.properties
│   │   └── db/migration/            # V1..V14 (Flyway)
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/                     # Axios clients por módulo
│   │   ├── hooks/                   # TanStack Query hooks
│   │   ├── stores/                  # Zustand (auth)
│   │   ├── components/
│   │   │   ├── ui/                  # shadcn/ui
│   │   │   ├── layout/              # AppLayout, NotificationBell, SearchBar
│   │   │   ├── ticket/              # TicketCard, SlaIndicator, RatingWidget
│   │   │   ├── chat/                # Chat components
│   │   │   └── category/
│   │   ├── pages/
│   │   ├── lib/                     # websocket, error, validations, queryClient
│   │   └── types/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── vite.config.ts
│
├── docker-compose.yml               # db + backend + frontend
├── docker-compose.dev.yml           # só banco
├── .env.example
├── .github/workflows/ci.yml         # CI: testes + build + docker
├── ARCHITECTURE.md
├── ROADMAP.md
└── README.md
```

## API Endpoints

Todas as rotas aceitam autenticação via header `Authorization: Bearer <token>`, exceto `/auth/login` e `/auth/register`. Quando acessadas pelo nginx, use prefixo `/api` (ex.: `http://localhost/api/tickets`); na API direta, sem prefixo (ex.: `http://localhost:8080/tickets`).

### Auth — `/auth`
| Método | Rota | Descrição |
|---|---|---|
| POST | `/auth/register` | Criar conta (role CLIENT) |
| POST | `/auth/login` | Login |
| POST | `/auth/refresh` | Renovar access token |
| POST | `/auth/logout` | Revoga refresh token |
| GET | `/auth/me` | Usuário autenticado |

### Tickets — `/tickets`
| Método | Rota | Descrição |
|---|---|---|
| POST | `/tickets` | Criar ticket |
| GET | `/tickets` | Listar com filtros (status, priority, categoryId) |
| GET | `/tickets/my` | Tickets do usuário autenticado |
| GET | `/tickets/{id}` | Detalhe |
| POST | `/tickets/{id}/assign` | Atribuir agente |
| PATCH | `/tickets/{id}/status` | Mudar status |
| POST | `/tickets/{id}/close` | Fechar |
| POST | `/tickets/{id}/transfer-agent` | Transferir para agente |
| POST | `/tickets/{id}/transfer-queue` | Transferir para fila |
| GET | `/tickets/{id}/activities` | Timeline |
| POST | `/tickets/{id}/rating` | Avaliar (CSAT, CLIENT criador) |
| GET | `/tickets/{id}/rating` | Ver avaliação |

### Chat — `/chat`
| Método | Rota | Descrição |
|---|---|---|
| POST | `/chat/sessions` | Iniciar sessão |
| GET | `/chat/sessions/{id}` | Detalhe da sessão |
| GET | `/chat/sessions/by-ticket/{ticketId}` | Sessão pelo ticket |
| POST | `/chat/sessions/{id}/end` | Encerrar sessão |
| GET | `/chat/sessions/{id}/messages` | Listar mensagens |
| GET | `/chat/sessions/{id}/messages/cursor` | Mensagens cursor‑based |
| POST | `/chat/messages` | Enviar mensagem (HTTP, fallback) |
| POST | `/chat/notes` | Nota interna |

### Filas e agentes — `/queues`, `/agents`
| Método | Rota | Descrição |
|---|---|---|
| GET | `/queues` | Listar filas |
| GET | `/queues/{id}` | Detalhe |
| POST | `/queues/{queueId}/agents/{agentId}` | Vincular agente |
| DELETE | `/queues/{queueId}/agents/{agentId}` | Desvincular agente |
| PATCH | `/agents/me/status` | Alterar próprio status (ONLINE/AWAY/OFFLINE) |
| GET | `/agents/{agentId}/status` | Consultar status |
| POST | `/agents/assign/{ticketId}` | Trigger manual de assign |

### Demais módulos
| Método | Rota | Descrição |
|---|---|---|
| GET | `/categories` | Categorias ativas |
| GET | `/categories/all` | Todas (ADMIN) |
| PUT | `/categories/{id}` | Editar |
| DELETE | `/categories/{id}` | Deletar (soft) |
| GET | `/canned-responses` | Listar |
| GET | `/canned-responses/search?q=` | Buscar |
| PUT | `/canned-responses/{id}` | Editar |
| DELETE | `/canned-responses/{id}` | Remover |
| GET | `/notifications` | Listar (paginado) |
| GET | `/notifications/unread-count` | Contagem não lidas |
| POST | `/notifications/{id}/read` | Marcar lida |
| POST | `/notifications/read-all` | Marcar todas lidas |
| GET | `/metrics/dashboard` | KPIs (ADMIN) |
| GET | `/search?q=&limit=` | Busca em tickets |
| GET | `/sla-policies` | Políticas de SLA ativas |

### WebSocket — `/ws`
Handshake com `Authorization: Bearer <token>`. Protocolo STOMP sobre SockJS.

| Destino | Direção | Descrição |
|---|---|---|
| `/topic/chat.{sessionId}` | subscribe | Mensagens realtime da sessão |
| `/topic/chat.{sessionId}.notes` | subscribe | Notas internas da sessão |
| `/topic/notifications.{userId}` | subscribe | Notificações push do usuário |
| `/app/chat.send` | send | Enviar mensagem |
| `/app/chat.note` | send | Enviar nota interna |

## Banco de dados — Migrations

14 migrations Flyway, executadas no boot.

| # | Descrição |
|---|---|
| V1 | users |
| V2 | tickets |
| V3 | queues e agent_status |
| V4 | chat sessions e messages |
| V5 | refresh_tokens |
| V6 | audit_log |
| V7 | soft delete (deleted_at) |
| V8 | categories |
| V9 | notas internas (is_internal) |
| V10 | ticket_activities |
| V11 | canned_responses |
| V12 | sla_policies |
| V13 | notifications |
| V14 | ticket_ratings (CSAT) |

## Testes e validação

### Builds e type check
```bash
cd backend && ./mvnw test            # JUnit 5 + Mockito + Testcontainers
cd backend && ./mvnw -DskipTests package
cd frontend && npx tsc --noEmit
cd frontend && npm run build
```

### Smoke tests (stack de pé via Docker)
```bash
# Health do Actuator
curl -s http://localhost:8080/actuator/health

# Cria usuário + login
curl -s -X POST http://localhost/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Teste","email":"teste@x.com","password":"Senha123!"}'

TOKEN=$(curl -s -X POST http://localhost/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"teste@x.com","password":"Senha123!"}' | jq -r .data.accessToken)

# Endpoints protegidos
curl -s -H "Authorization: Bearer $TOKEN" http://localhost/api/sla-policies
curl -s -H "Authorization: Bearer $TOKEN" http://localhost/api/notifications/unread-count
curl -s -H "Authorization: Bearer $TOKEN" http://localhost/api/tickets/my
```

### Fluxo visual (checklist)
No browser em <http://localhost>:
- [ ] Login com o usuário criado
- [ ] Criar ticket e ver `SlaIndicator` no card
- [ ] Abrir chat e trocar mensagens (verificar upgrade para WebSocket em DevTools → Network → `/ws`)
- [ ] Sino de notificações (top‑right) incrementa ao receber eventos
- [ ] Barra de busca devolve resultados com debounce
- [ ] Como ADMIN, acessar `/metrics` e ver o dashboard
- [ ] Fechar ticket e aparecer `RatingWidget` no detalhe

## CI

O workflow em `.github/workflows/ci.yml` roda em push para `main`/`develop` e PRs para `main`:

1. **`backend-test`** — testes do backend com PostgreSQL 16 como service; depois `mvnw package` sem testes.
2. **`frontend-test`** — `npm ci` + `tsc --noEmit` + `npm run build`.
3. **`docker-build`** — builda as duas imagens, dispara só em push para `main` depois dos dois jobs acima.

## Troubleshooting

**Backend não sobe — logs com `Could not resolve placeholder 'DATABASE_URL'`**
Significa que o Spring caiu no caminho do `application-prod.properties` sem env var satisfazendo. Confirme que o container tem `SPRING_DATASOURCE_URL` definido (`docker compose config | grep DATASOURCE`). Se estiver, reinicie o container: `docker compose up -d --force-recreate backend`.

**`JWT secret must be at least 32 characters`**
O `JwtProvider` valida o tamanho. Troque `JWT_SECRET` no `.env` por algo ≥ 32 caracteres (ex.: `openssl rand -base64 48`) e recrie o container.

**Healthcheck do backend fica `unhealthy`**
Veja `docker compose logs backend`. Confirme que o `application.properties` contém `management.endpoints.web.exposure.include=health` e que o Actuator está no `pom.xml`.

**Frontend abre mas `/api/*` retorna 502**
O backend não está healthy. `docker compose ps` e `docker compose logs backend`.

**Quero zerar o banco**
```bash
docker compose down -v   # apaga volume pgdata
docker compose up -d
```

**Erro `relation "users" does not exist`**
Flyway não rodou. Verifique `SPRING_FLYWAY_ENABLED=true` e que os arquivos `V*.sql` estão em `backend/src/main/resources/db/migration/`.

## Licença

MIT
