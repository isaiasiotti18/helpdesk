# ROADMAP.md — Melhorias, Refatorações e Novas Funcionalidades

## Legenda de prioridade
- 🔴 Alta — impacto direto em segurança, bugs ou UX crítica
- 🟡 Média — qualidade de código, DX, features importantes
- 🟢 Baixa — nice-to-have, escala futura

---

## 1. SEGURANÇA (Backend)

### 🔴 1.1 Tratamento de AuthorizationDeniedException
O `GlobalExceptionHandler` não trata exceções do Spring Security. O 403 retorna HTML, não `ApiResponse`.

### 🔴 1.2 Validação de ownership nos tickets
Qualquer usuário autenticado acessa qualquer ticket. CLIENT só deve ver tickets onde `createdBy = userId`. AGENT só tickets atribuídos a ele ou na sua fila.

### 🔴 1.3 Refresh token em banco
Hoje o refresh token é JWT stateless — funciona mesmo após logout. Salvar no banco com `revoked_at`. No logout, revogar. No `/auth/refresh`, verificar.

### 🟡 1.4 Rate limiting no login
Sem rate limit, brute force é possível. Bucket4j ou filtro simples contando tentativas por IP/email.

### 🟡 1.5 CORS configurado
Sem CORS definido no SecurityConfig. Em produção com domínios separados, quebra.

### 🟡 1.6 JWT secret validado no startup
Se `app.jwt.secret` < 32 chars, falhar na inicialização com mensagem clara.

### 🟡 1.7 Validação de role no assign
`Ticket.assignTo()` não verifica se o agente tem role AGENT. Um CLIENT poderia ser atribuído.

---

## 2. ARQUITETURA (Backend)

### 🔴 2.1 N+1 queries
`TicketResponse.from(ticket)` acessa `createdBy.getName()` e `assignedAgent.getName()`. 20 tickets = 40 queries extras. Usar `@EntityGraph` ou `JOIN FETCH`.

### 🔴 2.2 Specification JPA nos filtros
`TicketService.list()` tem if/else crescente. Usar `Specification<Ticket>` pra filtros composáveis.

### 🟡 2.3 Paginação cursor-based nas mensagens
Offset-based duplica/pula mensagens quando novas chegam. Trocar pra `GET /messages?before={lastMessageId}&limit=50`.

### 🟡 2.4 Auditoria (audit_log)
Tabela `audit_log` com `entity_type, entity_id, action, user_id, timestamp, payload_json`. Popular via `@EventListener` nos domain events existentes.

### 🟡 2.5 Soft delete
`deleted_at` em tickets e users. `@Where(clause = "deleted_at IS NULL")` ou filtro manual.

### 🟡 2.6 Entidade TicketAssignment
A tabela existe no SQL mas não como entidade JPA. Criar e popular no `assignTo()` e `transfer()` pra histórico de transferências.

### 🟢 2.7 Batch insert de mensagens
`AsyncMessagePersistence` salva uma por vez. Buffer de 10-50 mensagens a cada 100ms em alta carga.

### 🟢 2.8 Connection pool tuning (Neon)
HikariCP com defaults. Pra Neon: `maximumPoolSize=5`, `connectionTimeout=20000`, `idleTimeout=300000`.

---

## 3. TESTES (Backend)

### 🔴 3.1 Testes unitários
- `TicketStatus.canTransitionTo()` — todas transições válidas e inválidas
- `LeastActiveStrategy.selectAgent()` — lista vazia, um agente, empate
- `AuthService.login()` — credenciais válidas, inválidas, conta desativada
- `Ticket.assignTo()`, `Ticket.close()` — domain logic

### 🟡 3.2 Testes de integração
`@SpringBootTest` + Testcontainers. Fluxo: register → login → criar ticket → assign → chat.

### 🟡 3.3 Testes de carga (k6)
200 VUs, meta 100 req/s, p95 < 250ms. Cenários: criar ticket, listar, enviar mensagem.

---

## 4. FRONTEND — ARQUITETURA

### 🔴 4.1 Error boundaries
Criar `ErrorBoundary` global e por rota. Sem isso, qualquer erro derruba a tela inteira.

### 🔴 4.2 Loading states consistentes
Componente `LoadingSpinner` + wrapper `QueryGuard` que trata loading, error e empty state padronizado.

### 🔴 4.3 Interceptor de refresh token
Hoje 401 = logout. Correto: 401 → tentar refresh → se ok, repetir request original.

### 🟡 4.4 Toast notifications
Feedback: "Ticket criado", "Erro ao enviar", "Sessão encerrada". shadcn `toast` já tá instalado.

### 🟡 4.5 Optimistic updates no chat
Mensagem aparece na lista imediatamente com status "enviando". Confirma quando broadcast volta.

### 🟡 4.6 Tipagem do Axios error
Helper `getApiError(error)` que extrai mensagem real do backend.

### 🟡 4.7 Constants centralizadas
Status labels, priority labels duplicados em vários arquivos. Centralizar em `src/lib/constants.ts`.

### 🟢 4.8 React Query devtools
`@tanstack/react-query-devtools` em dev.

### 🟢 4.9 Componente FormField reutilizável
Login e Register quase idênticos. Wrapper genérico pra campos com label + input + error.

---

## 5. FRONTEND — UX

### 🟡 5.1 Responsive design (mobile)
Sidebar fixa não funciona em mobile. `Sheet` do shadcn pra hamburger menu.

### 🟡 5.2 Skeleton loading
`Skeleton` component ao invés de "Carregando...".

### 🟡 5.3 Empty states contextualizados
"Você não tem tickets ainda. Crie o primeiro!" com call-to-action.

### 🟡 5.4 Confirmação em ações destrutivas
`AlertDialog` do shadcn antes de fechar ticket ou encerrar chat.

### 🟡 5.5 Formatação de datas relativa
"há 5 minutos", "ontem às 19:50". `date-fns` com `formatDistanceToNow`.

### 🟢 5.6 Typing indicator no chat
Evento `chat.typing` via STOMP. "Agente está digitando..."

### 🟢 5.7 Toggle de status do agente
Na sidebar: ONLINE/OFFLINE com switch. Conecta no `PATCH /agents/me/status`.

---

## 6. INFRA

### 🟡 6.1 Docker Compose
Backend + Frontend + PostgreSQL local. Dev sem depender do Neon.

### 🟡 6.2 Dockerfile multi-stage
Backend: Maven build → JRE slim. Frontend: Node build → Nginx.

### 🟡 6.3 CI/CD (GitHub Actions)
Testes no PR, build Docker no merge pra main, deploy automático.

### 🟡 6.4 Variáveis de ambiente validadas
Falhar com mensagem clara se `DATABASE_URL` ou `JWT_SECRET` não definidas.

### 🟢 6.5 Logs estruturados (JSON)
Logback JSON encoder pra integrar com Grafana Loki.

### 🟢 6.6 Health check endpoint
Spring Actuator `/actuator/health` com status do banco.

---

## 7. NOVAS FUNCIONALIDADES — HELPDESK PROFISSIONAL

### 🔴 7.1 Categorias e Departamentos
Tickets pertencem a uma categoria (Financeiro, TI, RH) e podem ser roteados automaticamente pra fila correta.

```
categories
- id, name, description, queue_id (FK), is_active

tickets
- category_id (FK) → nova coluna
```

Impacto: auto-routing por categoria. Cliente seleciona categoria ao criar ticket → sistema coloca na fila certa.

---

### 🔴 7.2 SLA (Service Level Agreement)
Cada fila ou prioridade tem um SLA: tempo máximo de primeira resposta e tempo máximo de resolução.

```
sla_policies
- id, name, priority, first_response_minutes, resolution_minutes

tickets
- sla_policy_id (FK)
- first_response_at (TIMESTAMP)
- sla_first_response_deadline (TIMESTAMP)
- sla_resolution_deadline (TIMESTAMP)
- sla_breached (BOOLEAN)
```

Dashboard mostra tickets prestes a estourar SLA. Alertas visuais: verde (dentro), amarelo (próximo), vermelho (estourado).

---

### 🔴 7.3 Notas Internas
Agentes precisam trocar informações sobre o ticket sem que o cliente veja. Campo `is_internal` na mensagem.

```
messages
- is_internal (BOOLEAN DEFAULT FALSE)
```

Frontend: tab "Chat" e "Notas Internas". Notas aparecem com visual diferente (fundo amarelo). Cliente nunca vê.

---

### 🟡 7.4 Tags nos Tickets
Tags livres pra classificação e busca: "bug", "urgente", "reembolso", "produto-x".

```
tags
- id, name, color

ticket_tags
- ticket_id (FK), tag_id (FK)
```

Filtro por tag na listagem. Autocomplete ao digitar.

---

### 🟡 7.5 Respostas Prontas (Canned Responses)
Agentes usam respostas pré-definidas pra perguntas frequentes. Atalho no chat: digita `/` e seleciona.

```
canned_responses
- id, title, content, category, created_by (FK), is_shared (BOOLEAN)
```

Pessoais (só o agente vê) e compartilhadas (toda a equipe).

---

### 🟡 7.6 Anexos / Upload de Arquivos
Cliente e agente podem enviar arquivos (prints, PDFs, docs).

```
attachments
- id, message_id (FK), ticket_id (FK), file_name, file_url, file_size, mime_type, uploaded_at
```

Storage: S3, MinIO ou filesystem local. Limite de 10MB por arquivo. Preview inline pra imagens.

---

### 🟡 7.7 Histórico de Atividades do Ticket (Timeline)
Tudo que aconteceu no ticket em ordem cronológica: criação, atribuição, transferência, mudança de status, notas internas, mensagens.

```
ticket_activities
- id, ticket_id (FK), user_id (FK), action, detail_json, created_at
```

Actions: `CREATED`, `ASSIGNED`, `TRANSFERRED`, `STATUS_CHANGED`, `PRIORITY_CHANGED`, `NOTE_ADDED`, `MESSAGE_SENT`, `SLA_BREACHED`, `CLOSED`.

Frontend: timeline vertical no detalhe do ticket.

---

### 🟡 7.8 Dashboard de Métricas (Admin)
Painel com KPIs reais:
- Tickets abertos / em andamento / resolvidos hoje
- Tempo médio de primeira resposta
- Tempo médio de resolução
- % de SLA cumprido
- Tickets por categoria
- Carga por agente (gráfico de barras)
- Tickets criados por dia (gráfico de linha, últimos 30 dias)

Usar Recharts no frontend. Endpoint `GET /metrics/dashboard` no backend com queries agregadas.

---

### 🟡 7.9 Pesquisa Full-Text
Buscar tickets por título, descrição, conteúdo de mensagens. Indispensável quando tem milhares de tickets.

Opções:
- **MVP**: `ILIKE '%termo%'` no PostgreSQL (simples, lento em escala)
- **V2**: `tsvector` + `tsquery` do PostgreSQL (full-text nativo, bom até ~1M tickets)
- **V3**: Elasticsearch / Meilisearch (escala real)

---

### 🟡 7.10 Notificações
Notificar agente quando:
- Novo ticket atribuído
- Nova mensagem do cliente
- SLA prestes a estourar
- Ticket transferido pra ele

Notificar cliente quando:
- Ticket atribuído a agente
- Agente respondeu
- Ticket resolvido/fechado

```
notifications
- id, user_id (FK), type, title, content, ticket_id (FK), is_read, created_at
```

**MVP**: notificações in-app (badge no header + dropdown). Via WebSocket push.
**V2**: email (SendGrid/SES).
**V3**: push notification (browser/mobile).

---

### 🟡 7.11 Base de Conhecimento (Knowledge Base)
Artigos de autoajuda pra clientes resolverem sozinhos antes de abrir ticket.

```
kb_articles
- id, title, slug, content (markdown), category_id, author_id (FK), status (DRAFT/PUBLISHED), views, helpful_count, created_at, updated_at

kb_categories
- id, name, description, order
```

Frontend público (sem login). Busca. "Este artigo foi útil?" com contador.

Integração: ao criar ticket, sugerir artigos relacionados baseado no título.

---

### 🟡 7.12 Avaliação de Atendimento (CSAT)
Após fechar ticket, cliente avalia: 1-5 estrelas + comentário opcional.

```
ticket_ratings
- id, ticket_id (FK), user_id (FK), score (1-5), comment, created_at
```

Métricas: CSAT médio por agente, por fila, por período.

---

### 🟡 7.13 Transferência entre Filas
Agente transfere ticket pra outra fila (ex: de "Suporte Geral" pra "Financeiro"). O ticket sai do agente atual, entra na nova fila, e o algoritmo de atribuição redistribui.

Endpoint: `POST /tickets/{id}/transfer-queue`
Body: `{ "queueId": "..." }`

---

### 🟡 7.14 Múltiplos Canais de Entrada
Tickets criados por:
- **Web** (formulário, já existe)
- **Email** (inbound parsing: receber email → criar ticket automaticamente)
- **WhatsApp** (API do WhatsApp Business → webhook → criar ticket)
- **API pública** (outras plataformas criam tickets via REST)

```
tickets
- channel (WEB | EMAIL | WHATSAPP | API) → nova coluna
- external_ref (ID externo do canal)
```

---

### 🟢 7.15 Regras de Automação (Triggers)
Ações automáticas baseadas em condições:

Exemplos:
- "Se ticket com prioridade URGENT e sem agente há 5 min → escalar pra ADMIN"
- "Se ticket sem resposta há 24h → enviar lembrete ao agente"
- "Se ticket resolvido há 48h sem resposta do cliente → fechar automaticamente"

```
automation_rules
- id, name, event (TICKET_CREATED, TICKET_UPDATED, TIME_BASED), conditions_json, actions_json, is_active
```

MVP: Scheduler com `@Scheduled` que roda a cada minuto verificando regras TIME_BASED.

---

### 🟢 7.16 Multi-tenancy
Cada empresa (tenant) tem seus dados isolados. Necessário pra SaaS.

Opções:
- **Schema per tenant**: cada tenant tem schema separado no PostgreSQL
- **RLS (Row Level Security)**: uma tabela, filtro por `tenant_id` em toda query
- **Database per tenant**: máximo isolamento, maior custo

```
tenants
- id, name, slug, plan, settings_json, created_at

Toda tabela ganha:
- tenant_id (FK)
```

---

### 🟢 7.17 Webhooks de Saída
Permite que sistemas externos recebam eventos do helpdesk.

```
webhooks
- id, tenant_id, url, events (JSON array), secret, is_active

webhook_deliveries
- id, webhook_id, event, payload_json, status_code, delivered_at
```

Eventos: `ticket.created`, `ticket.closed`, `message.sent`, `sla.breached`.

---

### 🟢 7.18 Relatórios Exportáveis
Exportar dados em CSV/PDF:
- Tickets por período
- Performance por agente
- SLA compliance
- CSAT por período

Endpoint: `GET /reports/tickets?from=2026-01-01&to=2026-03-31&format=csv`

---

### 🟢 7.19 Painel do Cliente (Customer Portal)
Área self-service onde o cliente:
- Vê todos os seus tickets e status
- Abre novo ticket
- Acessa base de conhecimento
- Vê histórico de conversas

Hoje o frontend é uma única app. O portal do cliente poderia ser uma rota separada (`/portal`) com layout próprio, mais simples.

---

### 🟢 7.20 Integração com IA
- **Classificação automática**: ao criar ticket, IA sugere categoria e prioridade baseado no título/descrição
- **Sugestão de resposta**: agente recebe sugestão de resposta baseada na base de conhecimento
- **Resumo de ticket**: IA resume conversa longa em 3 linhas pra agente que assume transferência
- **Chatbot de primeira linha**: antes de criar ticket, bot tenta resolver com artigos da KB

---

## ORDEM SUGERIDA DE IMPLEMENTAÇÃO

### Fase 1 — Qualidade (2 semanas)
Items: 1.1, 1.2, 2.1, 2.2, 3.1, 4.1, 4.2, 4.3, 4.4

### Fase 2 — Features Core (3 semanas)
Items: 7.1 (Categorias), 7.3 (Notas Internas), 7.7 (Timeline), 7.5 (Canned Responses), 7.10 (Notificações), 5.1 (Responsive)

### Fase 3 — Profissionalização (3 semanas)
Items: 7.2 (SLA), 7.4 (Tags), 7.6 (Anexos), 7.8 (Dashboard Métricas), 7.12 (CSAT), 7.9 (Busca)

### Fase 4 — SaaS (4 semanas)
Items: 7.16 (Multi-tenancy), 7.11 (Knowledge Base), 7.14 (Multi-canal), 7.19 (Portal do Cliente), 6.1-6.3 (Docker + CI/CD)

### Fase 5 — Diferenciação (ongoing)
Items: 7.15 (Automações), 7.17 (Webhooks), 7.18 (Relatórios), 7.20 (IA)
