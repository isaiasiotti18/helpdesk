# HelpDesk SaaS

Sistema de gestão de chamados + chat realtime. Monolito modular com Java 21, Spring Boot, React 18 e PostgreSQL.

## Stack

**Backend**: Java 21, Spring Boot 3.3+, Spring Security (JWT), Spring WebSocket (STOMP), Spring Data JPA, Flyway

**Frontend**: React 18, TypeScript, Vite, Tailwind CSS, shadcn/ui, Zustand, TanStack React Query, React Hook Form + Zod

**Banco**: PostgreSQL 16

**Infra**: Docker, Docker Compose, GitHub Actions

## Funcionalidades

### Core
- **Tickets** — Criação, atribuição, ciclo de vida com máquina de estados (OPEN → IN_QUEUE → IN_PROGRESS → RESOLVED → CLOSED)
- **Chat Realtime** — WebSocket (STOMP + SockJS) com persistência assíncrona em batch
- **Filas de Atendimento** — Auto-assign via algoritmo Least Active Agent
- **Categorias e Departamentos** — Roteamento automático por categoria vinculada à fila
- **Notas Internas** — Mensagens visíveis apenas para agentes, com canal WebSocket separado
- **Transferência** — Transferir ticket para outro agente ou outra fila

### Avançado
- **SLA** — Políticas por prioridade, deadlines automáticos, verificação de breach a cada 60s
- **Notificações** — Push realtime via WebSocket + persistência + badge de não lidas
- **Dashboard de Métricas** — KPIs (tempo médio resposta, SLA compliance, carga por agente, tickets/dia)
- **CSAT** — Avaliação por estrelas (1-5) após fechamento do ticket
- **Busca Full-Text** — Busca por título e descrição com debounce e dropdown de resultados
- **Respostas Prontas** — Atalhos de texto para agentes (digitar `/` no chat abre o picker)
- **Timeline de Atividades** — Histórico completo de ações no ticket

### Segurança
- JWT com access token (1h) + refresh token (7d) com rotação
- Refresh tokens hasheados (SHA-256) no banco
- Rate limiting no login (5 tentativas / 5 min por email)
- Validação de ownership (cliente só vê seus tickets)
- CORS configurável por ambiente
- Soft delete em tickets e usuários

### Arquitetura
- Modular Monolith com DDD-lite
- Optimistic locking (`@Version`) nos tickets
- JPA Specifications para filtros composáveis
- Paginação cursor-based nas mensagens
- Batch insert de mensagens (buffer + flush a cada 200ms)
- Audit log assíncrono via domain events
- Connection pool tuning para Neon (HikariCP)

### Frontend
- Error boundaries (global + por rota)
- Refresh token interceptor com fila de requests
- Optimistic updates no chat
- Skeleton loading e empty states contextualizados
- Toast notifications (Sonner)
- Responsive design (sidebar com Sheet no mobile)
- Datas relativas em pt-BR (date-fns)

## Quick Start

### Com Docker (recomendado)

```bash
git clone https://github.com/seu-usuario/helpdesk.git
cd helpdesk

# Subir tudo
docker compose up -d

# Acesse
# Frontend: http://localhost
# Backend:  http://localhost:8080
```

### Desenvolvimento local

```bash
# 1. Subir só o banco
docker compose -f docker-compose.dev.yml up -d

# 2. Backend
cd backend
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
# Editar com suas credenciais do banco
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Frontend
cd frontend
npm install
npm run dev
```

Frontend roda em `http://localhost:5173` com proxy automático para o backend.

## Variáveis de Ambiente

| Variável | Descrição | Default |
|----------|-----------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://db:5432/helpdesk` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `helpdesk` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `helpdesk` |
| `APP_JWT_SECRET` | Secret do JWT (mín. 32 chars) | — |
| `APP_CORS_ALLOWED_ORIGINS` | Origens permitidas (separadas por vírgula) | `http://localhost` |
| `SPRING_PROFILES_ACTIVE` | Perfil do Spring | `dev` |

## Estrutura do Projeto

```
helpdesk/
├── backend/
│   ├── src/main/java/com/helpdesk/backend/
│   │   ├── config/                    # Security, WebSocket, Async, CORS
│   │   ├── shared/                    # Exceptions, DTOs, JWT, Audit
│   │   └── modules/
│   │       ├── auth/                  # Login, registro, refresh token
│   │       ├── user/                  # Entidade User, roles
│   │       ├── ticket/                # Tickets, SLA, Timeline, CSAT, Busca
│   │       ├── queue/                 # Filas, AgentStatus, Assignment
│   │       ├── chat/                  # WebSocket, mensagens, notas, respostas prontas
│   │       ├── category/              # Categorias e departamentos
│   │       ├── notification/          # Notificações realtime
│   │       └── metrics/               # Dashboard de métricas
│   ├── src/main/resources/
│   │   └── db/migration/              # V1 a V14 (Flyway)
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/                       # Axios clients por módulo
│   │   ├── hooks/                     # React Query hooks
│   │   ├── stores/                    # Zustand (auth)
│   │   ├── components/                # UI components
│   │   ├── pages/                     # Route pages
│   │   ├── lib/                       # Utils, constants, validations, websocket
│   │   └── types/                     # TypeScript types
│   ├── Dockerfile
│   └── nginx.conf
│
├── docker-compose.yml                 # Full stack (db + backend + frontend)
├── docker-compose.dev.yml             # Só banco (dev local)
├── .github/workflows/ci.yml           # CI: testes + build + docker
└── README.md
```

## API Endpoints

### Auth
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/auth/register` | Criar conta |
| POST | `/auth/login` | Login |
| POST | `/auth/refresh` | Renovar token |
| POST | `/auth/logout` | Logout (revoga refresh token) |
| GET | `/auth/me` | Usuário atual |

### Tickets
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/tickets` | Criar ticket |
| GET | `/tickets` | Listar (com filtros: status, priority, categoryId) |
| GET | `/tickets/{id}` | Detalhe |
| PUT | `/tickets/{id}/assign` | Atribuir agente |
| PUT | `/tickets/{id}/close` | Fechar |
| POST | `/tickets/{id}/transfer-agent` | Transferir para agente |
| POST | `/tickets/{id}/transfer-queue` | Transferir para fila |
| GET | `/tickets/{id}/activities` | Timeline de atividades |
| POST | `/tickets/{id}/rating` | Avaliar (CSAT) |
| GET | `/tickets/{id}/rating` | Ver avaliação |

### Chat
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/chat/sessions` | Iniciar sessão |
| GET | `/chat/sessions/{id}/messages` | Listar mensagens |
| GET | `/chat/sessions/{id}/messages/cursor` | Mensagens (cursor-based) |
| POST | `/chat/notes` | Enviar nota interna |
| WS | `/ws` → `/topic/chat.{sessionId}` | Mensagens realtime |
| WS | `/ws` → `/topic/chat.{sessionId}.notes` | Notas internas |
| WS | `/ws` → `/app/chat.send` | Enviar mensagem |

### Outros
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/categories` | Listar categorias ativas |
| GET | `/canned-responses` | Respostas prontas |
| GET | `/notifications` | Notificações do usuário |
| GET | `/metrics/dashboard` | Dashboard (ADMIN) |
| GET | `/search?q=` | Buscar tickets |
| GET | `/sla-policies` | Políticas de SLA |

## Testes

```bash
# Unitários + Integração
cd backend && ./mvnw test

# Type check do frontend
cd frontend && npx tsc --noEmit
```

## Migrations

14 migrations Flyway, executadas automaticamente no boot:

| Migration | Descrição |
|-----------|-----------|
| V1 | Tabela users |
| V2 | Tabela tickets |
| V3 | Filas e agent_status |
| V4 | Chat sessions e messages |
| V5 | Refresh tokens |
| V6 | Audit log |
| V7 | Soft delete |
| V8 | Categorias |
| V9 | Notas internas (is_internal) |
| V10 | Timeline de atividades |
| V11 | Respostas prontas |
| V12 | SLA policies |
| V13 | Notificações |
| V14 | CSAT (ticket_ratings) |

## Licença

MIT
