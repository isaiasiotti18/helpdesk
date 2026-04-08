# MEMORY.md

Registro de decisões, contexto e evolução do projeto.

---

## Decisões Arquiteturais

| # | Decisão | Motivo | Data |
|---|---------|--------|------|
| 1 | Modular Monolith | Complexidade de microservices não se justifica para MVP. Módulos comunicam via eventos internos. | Início |
| 2 | DDD-lite | Domain + Application + Infra + Presentation por módulo. Sem Aggregates complexos no MVP. | Início |
| 3 | Optimistic Locking | Resolve race condition de assignment sem pessimistic lock (melhor throughput). | Início |
| 4 | Async chat persistence | Mensagens de chat persistidas em thread pool separado para não bloquear WebSocket. | Início |
| 5 | Flyway migrations | Versionamento explícito do schema. Neon suporta sem problemas. | Início |
| 6 | JWT stateless | Sem session server-side. Token carrega role e userId. Refresh token com rotação. | Início |
| 7 | Strategy pattern na fila | Permite trocar algoritmo (RoundRobin, LeastActive, SLA) sem alterar QueueService. | Início |
| 8 | STOMP sobre WebSocket | Suporte nativo do Spring, protocolo com subscribe/publish, mais simples que raw WS. | Início |

## Stack Choices

| Componente | Escolha | Alternativa descartada | Por quê |
|-----------|---------|----------------------|---------|
| ORM | Spring Data JPA + Hibernate | jOOQ | JPA resolve bem para este domínio. jOOQ é overkill aqui. |
| Migrations | Flyway | Liquibase | Flyway é mais simples, SQL puro. |
| Auth | JWT custom + Spring Security | Auth0/Keycloak | Menos dependência externa no MVP. Adicionar OAuth depois. |
| State mgmt | Zustand | Redux | Menos boilerplate, API mais simples. |
| Styling | Tailwind + shadcn | MUI, Ant Design | Mais leve, customizável, controle total. |
| WebSocket | STOMP.js + SockJS | Socket.io | Integração nativa com Spring WebSocket. |

## Contexto do Projeto

- **Tipo**: Projeto pessoal com intenção de comercialização futura (SaaS).
- **Público alvo**: Empresas pequenas/médias que precisam de helpdesk interno ou externo.
- **Diferencial técnico**: Chat realtime integrado ao ciclo do ticket + algoritmo de distribuição inteligente.
- **Prioridade**: Funcionar bem > Arquitetura perfeita. Ship fast, refactor later.

## Riscos Conhecidos

| Risco | Mitigação |
|-------|-----------|
| WebSocket em Neon (serverless) | Neon é só banco. WS fica no Spring. Nenhum problema. |
| Chat sem Redis (single instance) | Funciona para MVP. Adicionar Redis pub/sub na V2 para multi-instance. |
| Mensagens perdidas sem ACK | Implementar message_id + client retry no MVP. |
| Performance de busca em messages | Índice em (session_id, sent_at). Paginação cursor-based. |

## TODO (tracking manual)

- [ ] Setup monorepo (backend + frontend)
- [ ] Docker Compose com PostgreSQL local
- [ ] Flyway migrations base
- [ ] Módulo auth (login, register, JWT)
- [ ] Módulo user (CRUD, roles)
- [ ] Módulo ticket (CRUD, status machine)
- [ ] Módulo queue (assignment)
- [ ] Módulo chat (WebSocket + persistence)
- [ ] Frontend auth flow
- [ ] Frontend ticket list + create
- [ ] Frontend chat UI
- [ ] Testes unitários core
- [ ] Testes de integração API
- [ ] k6 load tests
- [ ] Docker build + deploy
