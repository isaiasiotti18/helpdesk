# HelpDesk

Sistema de gestão de chamados com chat em tempo real, filas inteligentes de atendimento e métricas operacionais.

## Stack

**Backend**: Java 21 · Spring Boot 3 · Spring Security · Spring WebSocket · Flyway  
**Frontend**: React 18 · TypeScript · Vite · Tailwind CSS · shadcn/ui · Zustand  
**Database**: PostgreSQL 16 (Neon)  
**Infra**: Docker · Docker Compose · Nginx

## Quick Start

```bash
# Clone
git clone https://github.com/seu-user/helpdesk.git
cd helpdesk

# Subir infra local (PostgreSQL)
docker compose up -d

# Backend
cd backend
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
# Editar credenciais do banco
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`

## Arquitetura

Modular Monolith com DDD-lite. Detalhes em [ARCHITECTURE.md](docs/ARCHITECTURE.md).

```
Frontend ──HTTP/WS──▶ Spring Boot API ──▶ PostgreSQL
                      ├── auth
                      ├── user
                      ├── ticket
                      ├── queue
                      ├── chat
                      └── metrics
```

## Roadmap

### Fase 1 — MVP Core ✦
> Meta: fluxo completo funcionando end-to-end.

| Sprint | Entrega | Detalhe |
|--------|---------|---------|
| **S1** | Setup & Auth | Monorepo, Docker Compose, Flyway, módulo auth (register/login/JWT), Spring Security |
| **S2** | Tickets | CRUD de tickets, status machine (OPEN→IN_PROGRESS→RESOLVED→CLOSED), optimistic locking, listagem com filtros |
| **S3** | Queue & Assignment | Módulo queue, AgentStatus, algoritmo Least Active Agent, auto-assign ao criar ticket |
| **S4** | Chat Realtime | WebSocket STOMP, ChatSession, Messages, persistência async, auth no handshake |
| **S5** | Frontend Core | Login, Dashboard, lista de tickets, criar ticket, tela de chat |
| **S6** | Integração E2E | Fluxo completo: criar ticket → assign → chat → close. Testes de integração. |

### Fase 2 — Refinamento
| Item | Detalhe |
|------|---------|
| Transferência de tickets | Agente transfere para outro agente/fila |
| Prioridade e SLA | Score de atribuição baseado em prioridade + tempo de espera |
| Notificações | Notificação in-app quando ticket é atribuído ou mensagem chega |
| Paginação cursor-based | Messages e tickets com cursor ao invés de offset |
| Testes de carga | k6: 200 VUs, meta 100 req/s, p95 < 250ms |

### Fase 3 — Escala e Produção
| Item | Detalhe |
|------|---------|
| Redis | Cache (tickets, queue status) + Pub/Sub (WebSocket multi-instance) |
| Métricas dashboard | Admin: tempo médio resposta, tickets/hora, agentes ativos |
| OAuth | Login com Google/GitHub via Spring Security OAuth2 |
| CI/CD | GitHub Actions: test → build → push image → deploy |
| Monitoring | Prometheus + Grafana (latência, errors, WS connections) |

### Fase 4 — SaaS
| Item | Detalhe |
|------|---------|
| Multi-tenancy | Schema per tenant ou RLS |
| Billing | Planos, limites por tenant |
| Kafka | Event-driven para auditoria, analytics, integrações |
| Kubernetes | Horizontal scaling, zero downtime deploys |

## Testes

```bash
# Unit + Integration
cd backend && ./mvnw test

# Frontend
cd frontend && npm test

# Load (k6)
k6 run infra/k6/full-flow.js
```

## Docs

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) — Arquitetura, schema, patterns
- [MEMORY.md](docs/MEMORY.md) — Decisões e tracking
- [DESCRIPTION.md](docs/DESCRIPTION.md) — Descrição do produto

## License

MIT
