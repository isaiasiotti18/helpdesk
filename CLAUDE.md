# CLAUDE.md

## Projeto
HelpDesk SaaS — sistema de gestão de chamados + chat realtime.

## Stack
- **Backend**: Java 21, Spring Boot 3.3+, Spring Security, Spring Data JPA, Spring WebSocket (STOMP)
- **Frontend**: React 18, Vite, TypeScript, Tailwind CSS, shadcn/ui
- **Banco**: PostgreSQL 16 (Neon)
- **Infra**: Docker, Docker Compose

## Arquitetura
Modular Monolith com DDD-lite. Módulos: auth, user, ticket, queue, chat, metrics.

## Convenções

### Backend
- Package root: `com.helpdesk`
- Módulos em `com.helpdesk.modules.<module>.{domain,application,infra,presentation}`
- Shared em `com.helpdesk.shared`
- Entidades usam `@Version` para optimistic locking
- Eventos internos via `ApplicationEventPublisher` (não Kafka no MVP)
- Chat messages persistidas async via `@Async` + thread pool
- DTOs com Java Records
- Exceções mapeadas via `@ControllerAdvice`

### Frontend
- Path aliases: `@/` → `src/`
- Componentes em `src/components/ui/` (shadcn) e `src/components/` (custom)
- Estado global: Zustand
- HTTP: Axios com interceptors para JWT
- WebSocket: SockJS + STOMP.js
- Rotas: React Router v6

### Banco
- Migrations: Flyway (prefixo `V{n}__description.sql`)
- Naming: snake_case, plural (users, tickets, messages)
- Soft delete: `deleted_at` timestamp onde aplicável
- Índices explícitos para queries frequentes

### Testes
- Backend: JUnit 5 + Mockito (unit), @SpringBootTest + Testcontainers (integration)
- Frontend: Vitest + Testing Library
- Carga: k6

### Git
- Commits: Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`)
- Branches: `main`, `develop`, `feat/<nome>`, `fix/<nome>`
- PR obrigatório para main

## Comandos
```bash
# Backend
cd backend && ./mvnw spring-boot:run

# Frontend
cd frontend && npm run dev

# Docker
docker compose up -d

# Testes
cd backend && ./mvnw test
cd frontend && npm test

# Migrations
# Flyway roda automático no boot
```

## Regras
- Nunca expor entidades JPA direto na API — sempre usar DTOs
- Nunca salvar senha em plain text — bcrypt via Spring Security
- WebSocket autenticado via JWT no handshake
- Todo endpoint protegido exceto `/auth/login` e `/auth/register`
- Logs estruturados (JSON) em produção
