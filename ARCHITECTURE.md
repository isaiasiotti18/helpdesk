# ARCHITECTURE.md

## 1. Visão Geral

```
┌─────────────────────────────────────────────┐
│                  Frontend                   │
│           React + Vite + TypeScript         │
│          Tailwind CSS + shadcn/ui           │
└──────────────┬──────────────┬───────────────┘
               │ REST (HTTP)  │ WebSocket (STOMP)
               ▼              ▼
┌─────────────────────────────────────────────┐
│              Spring Boot API                │
│                                             │
│  ┌────────┐ ┌────────┐ ┌────────┐           │
│  │  Auth  │ │ Ticket │ │  Chat  │           │
│  └────────┘ └────────┘ └────────┘           │
│  ┌────────┐ ┌────────┐ ┌────────┐           │
│  │  User  │ │ Queue  │ │Metrics │           │
│  └────────┘ └────────┘ └────────┘           │
│                                             │
│  Spring Events (ApplicationEventPublisher)  │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   PostgreSQL    │
              │     (Neon)      │
              └─────────────────┘
```

**Decisão**: Modular Monolith, não microservices. Para MVP e até ~10k users simultâneos, monolith bem estruturado performa melhor e tem 10x menos complexidade operacional. Módulos se comunicam via eventos internos — quando precisar escalar, extrai módulo para serviço separado sem reescrever lógica.

---

## 2. Módulos e Responsabilidades

| Módulo | Responsabilidade | Depende de |
|--------|-----------------|------------|
| **auth** | Login, registro, JWT, refresh token | user |
| **user** | CRUD de usuários, roles (CLIENT, AGENT, ADMIN) | — |
| **ticket** | Criação, atualização, ciclo de vida do chamado | user, queue |
| **queue** | Fila de atendimento, algoritmo de atribuição | user, ticket |
| **chat** | WebSocket, sessões de chat, mensagens | user, ticket |
| **metrics** | Métricas operacionais (tempo resposta, tickets/hora) | ticket, chat |

**Regra**: módulos nunca acessam repositórios de outros módulos. Comunicação entre módulos via:

1. Interfaces de serviço (Application layer expõe)
2. Eventos de domínio (`TicketCreatedEvent`, `TicketAssignedEvent`, etc.)

---

## 3. Database Schema

```sql
-- ============================================
-- USERS
-- ============================================
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('CLIENT', 'AGENT', 'ADMIN')),
    avatar_url    VARCHAR(500),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================
-- QUEUES (filas de atendimento)
-- ============================================
CREATE TABLE queues (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   TEXT,
    max_agents    INT          NOT NULL DEFAULT 10,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Agentes vinculados a filas (N:N)
CREATE TABLE queue_agents (
    queue_id      UUID NOT NULL REFERENCES queues(id),
    agent_id      UUID NOT NULL REFERENCES users(id),
    joined_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (queue_id, agent_id)
);

-- ============================================
-- TICKETS
-- ============================================
CREATE TABLE tickets (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
                  CHECK (status IN ('OPEN','IN_QUEUE','IN_PROGRESS','TRANSFERRED','RESOLVED','CLOSED')),
    priority      VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM'
                  CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    queue_id      UUID         REFERENCES queues(id),
    created_by    UUID         NOT NULL REFERENCES users(id),
    assigned_agent UUID        REFERENCES users(id),
    version       INT          NOT NULL DEFAULT 0,  -- optimistic locking
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    closed_at     TIMESTAMP
);

-- ============================================
-- TICKET ASSIGNMENTS (histórico de atribuições)
-- ============================================
CREATE TABLE ticket_assignments (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id     UUID      NOT NULL REFERENCES tickets(id),
    agent_id      UUID      NOT NULL REFERENCES users(id),
    action        VARCHAR(20) NOT NULL CHECK (action IN ('ASSIGNED','TRANSFERRED','RELEASED')),
    assigned_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at      TIMESTAMP
);

-- ============================================
-- AGENT STATUS (estado do agente em tempo real)
-- ============================================
CREATE TABLE agent_status (
    agent_id       UUID PRIMARY KEY REFERENCES users(id),
    status         VARCHAR(20) NOT NULL DEFAULT 'OFFLINE'
                   CHECK (status IN ('ONLINE','BUSY','AWAY','OFFLINE')),
    active_tickets INT         NOT NULL DEFAULT 0,
    max_tickets    INT         NOT NULL DEFAULT 5,
    last_seen      TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ============================================
-- CHAT SESSIONS
-- ============================================
CREATE TABLE chat_sessions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id     UUID      NOT NULL REFERENCES tickets(id),
    started_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at      TIMESTAMP
);

-- ============================================
-- MESSAGES
-- ============================================
CREATE TABLE messages (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id    UUID         NOT NULL REFERENCES chat_sessions(id),
    sender_id     UUID         NOT NULL REFERENCES users(id),
    content       TEXT         NOT NULL,
    message_type  VARCHAR(20)  NOT NULL DEFAULT 'TEXT'
                  CHECK (message_type IN ('TEXT','SYSTEM','FILE')),
    sent_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ============================================
-- ÍNDICES CRÍTICOS
-- ============================================
CREATE INDEX idx_tickets_status          ON tickets(status);
CREATE INDEX idx_tickets_assigned_agent  ON tickets(assigned_agent);
CREATE INDEX idx_tickets_queue_id        ON tickets(queue_id);
CREATE INDEX idx_tickets_created_by      ON tickets(created_by);
CREATE INDEX idx_tickets_created_at      ON tickets(created_at DESC);

CREATE INDEX idx_messages_session_id     ON messages(session_id);
CREATE INDEX idx_messages_sent_at        ON messages(sent_at);

CREATE INDEX idx_ticket_assignments_ticket ON ticket_assignments(ticket_id);
CREATE INDEX idx_ticket_assignments_agent  ON ticket_assignments(agent_id);

CREATE INDEX idx_agent_status_status     ON agent_status(status);

-- Índice composto para query mais frequente: "tickets abertos na minha fila"
CREATE INDEX idx_tickets_queue_status    ON tickets(queue_id, status);
```

### Diagrama ER (relações)

```
users 1───N tickets (created_by)
users 1───N tickets (assigned_agent)
users 1───N ticket_assignments
users 1───1 agent_status
users N───N queues (via queue_agents)

queues 1───N tickets

tickets 1───N ticket_assignments
tickets 1───1 chat_sessions

chat_sessions 1───N messages
users 1───N messages (sender_id)
```

---

## 4. Estrutura de Pastas

```
helpdesk/
├── backend/
│   ├── src/main/java/com/helpdesk/
│   │   ├── HelpDeskApplication.java
│   │   │
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── AsyncConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   └── JacksonConfig.java
│   │   │
│   │   ├── shared/
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── ConcurrencyException.java
│   │   │   ├── event/
│   │   │   │   └── DomainEvent.java
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java
│   │   │   │   └── PageResponse.java
│   │   │   └── security/
│   │   │       ├── JwtProvider.java
│   │   │       ├── JwtFilter.java
│   │   │       └── CurrentUser.java
│   │   │
│   │   └── modules/
│   │       ├── auth/
│   │       │   ├── domain/
│   │       │   │   └── AuthService.java            (interface)
│   │       │   ├── application/
│   │       │   │   ├── AuthServiceImpl.java
│   │       │   │   ├── LoginCommand.java            (record)
│   │       │   │   ├── RegisterCommand.java         (record)
│   │       │   │   └── AuthResponse.java            (record)
│   │       │   └── presentation/
│   │       │       └── AuthController.java
│   │       │
│   │       ├── user/
│   │       │   ├── domain/
│   │       │   │   ├── User.java                    (entity)
│   │       │   │   ├── Role.java                    (enum)
│   │       │   │   └── UserRepository.java          (interface)
│   │       │   ├── application/
│   │       │   │   ├── UserService.java
│   │       │   │   ├── UserResponse.java            (record)
│   │       │   │   └── UpdateUserCommand.java       (record)
│   │       │   ├── infra/
│   │       │   │   └── JpaUserRepository.java
│   │       │   └── presentation/
│   │       │       └── UserController.java
│   │       │
│   │       ├── ticket/
│   │       │   ├── domain/
│   │       │   │   ├── Ticket.java                  (entity)
│   │       │   │   ├── TicketStatus.java            (enum)
│   │       │   │   ├── Priority.java                (enum)
│   │       │   │   ├── TicketAssignment.java        (entity)
│   │       │   │   ├── TicketRepository.java        (interface)
│   │       │   │   └── event/
│   │       │   │       ├── TicketCreatedEvent.java
│   │       │   │       ├── TicketAssignedEvent.java
│   │       │   │       └── TicketClosedEvent.java
│   │       │   ├── application/
│   │       │   │   ├── TicketService.java
│   │       │   │   ├── CreateTicketCommand.java     (record)
│   │       │   │   ├── TicketResponse.java          (record)
│   │       │   │   └── TicketListResponse.java      (record)
│   │       │   ├── infra/
│   │       │   │   └── JpaTicketRepository.java
│   │       │   └── presentation/
│   │       │       └── TicketController.java
│   │       │
│   │       ├── queue/
│   │       │   ├── domain/
│   │       │   │   ├── Queue.java                   (entity)
│   │       │   │   ├── AgentStatus.java             (entity)
│   │       │   │   ├── AssignmentStrategy.java      (interface)
│   │       │   │   ├── QueueRepository.java         (interface)
│   │       │   │   └── AgentStatusRepository.java   (interface)
│   │       │   ├── application/
│   │       │   │   ├── QueueService.java
│   │       │   │   ├── AssignmentService.java
│   │       │   │   └── LeastActiveStrategy.java     (implements AssignmentStrategy)
│   │       │   ├── infra/
│   │       │   │   ├── JpaQueueRepository.java
│   │       │   │   └── JpaAgentStatusRepository.java
│   │       │   └── presentation/
│   │       │       └── QueueController.java
│   │       │
│   │       ├── chat/
│   │       │   ├── domain/
│   │       │   │   ├── ChatSession.java             (entity)
│   │       │   │   ├── Message.java                 (entity)
│   │       │   │   ├── MessageType.java             (enum)
│   │       │   │   ├── ChatSessionRepository.java   (interface)
│   │       │   │   └── MessageRepository.java       (interface)
│   │       │   ├── application/
│   │       │   │   ├── ChatService.java
│   │       │   │   ├── SendMessageCommand.java      (record)
│   │       │   │   └── MessageResponse.java         (record)
│   │       │   ├── infra/
│   │       │   │   ├── JpaChatSessionRepository.java
│   │       │   │   ├── JpaMessageRepository.java
│   │       │   │   └── AsyncMessagePersistence.java
│   │       │   └── presentation/
│   │       │       ├── ChatController.java          (REST)
│   │       │       └── ChatWebSocketHandler.java    (STOMP)
│   │       │
│   │       └── metrics/
│   │           ├── application/
│   │           │   ├── MetricsService.java
│   │           │   └── DashboardResponse.java       (record)
│   │           └── presentation/
│   │               └── MetricsController.java
│   │
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/
│   │       ├── V1__create_users.sql
│   │       ├── V2__create_queues.sql
│   │       ├── V3__create_tickets.sql
│   │       ├── V4__create_chat.sql
│   │       └── V5__create_indexes.sql
│   │
│   ├── src/test/java/com/helpdesk/
│   │   └── modules/
│   │       ├── ticket/
│   │       │   ├── application/TicketServiceTest.java
│   │       │   └── presentation/TicketControllerIT.java
│   │       ├── queue/
│   │       │   └── application/AssignmentServiceTest.java
│   │       └── chat/
│   │           └── application/ChatServiceTest.java
│   │
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx
│   │   ├── routes.tsx
│   │   │
│   │   ├── api/
│   │   │   ├── client.ts              (axios instance)
│   │   │   ├── auth.ts
│   │   │   ├── tickets.ts
│   │   │   ├── chat.ts
│   │   │   └── queues.ts
│   │   │
│   │   ├── hooks/
│   │   │   ├── useAuth.ts
│   │   │   ├── useWebSocket.ts
│   │   │   ├── useTickets.ts
│   │   │   └── useChatMessages.ts
│   │   │
│   │   ├── stores/
│   │   │   ├── authStore.ts
│   │   │   └── chatStore.ts
│   │   │
│   │   ├── components/
│   │   │   ├── ui/                     (shadcn)
│   │   │   ├── layout/
│   │   │   │   ├── Sidebar.tsx
│   │   │   │   ├── Header.tsx
│   │   │   │   └── AppLayout.tsx
│   │   │   ├── ticket/
│   │   │   │   ├── TicketCard.tsx
│   │   │   │   ├── TicketList.tsx
│   │   │   │   ├── TicketForm.tsx
│   │   │   │   └── TicketDetail.tsx
│   │   │   └── chat/
│   │   │       ├── ChatWindow.tsx
│   │   │       ├── MessageBubble.tsx
│   │   │       ├── MessageInput.tsx
│   │   │       └── TypingIndicator.tsx
│   │   │
│   │   ├── pages/
│   │   │   ├── LoginPage.tsx
│   │   │   ├── RegisterPage.tsx
│   │   │   ├── DashboardPage.tsx
│   │   │   ├── TicketsPage.tsx
│   │   │   ├── TicketDetailPage.tsx
│   │   │   ├── ChatPage.tsx
│   │   │   └── AdminPage.tsx
│   │   │
│   │   ├── types/
│   │   │   └── index.ts
│   │   │
│   │   └── lib/
│   │       ├── websocket.ts
│   │       └── utils.ts
│   │
│   ├── index.html
│   ├── vite.config.ts
│   ├── tailwind.config.ts
│   ├── tsconfig.json
│   ├── package.json
│   └── Dockerfile
│
├── infra/
│   ├── docker-compose.yml
│   ├── docker-compose.prod.yml
│   ├── nginx/
│   │   └── nginx.conf
│   └── k6/
│       ├── create-ticket.js
│       ├── list-tickets.js
│       ├── chat-messages.js
│       └── full-flow.js
│
├── docs/
│   ├── ARCHITECTURE.md          (este arquivo)
│   ├── MEMORY.md
│   └── DESCRIPTION.md
│
├── CLAUDE.md
├── README.md
├── .gitignore
└── docker-compose.yml
```

---

## 5. Patterns a Seguir

### ✅ Usar

| Pattern | Onde | Por quê |
|---------|------|---------|
| **Optimistic Locking** | Ticket entity (`@Version`) | Evita dois agentes pegarem o mesmo ticket |
| **Domain Events** | TicketCreated, TicketAssigned | Desacopla módulos, facilita extração futura |
| **Strategy Pattern** | AssignmentStrategy | Troca de algoritmo de fila sem alterar QueueService |
| **Repository Pattern** | Todas as entidades | Interface no domain, implementação na infra |
| **Command Pattern** | CreateTicketCommand, SendMessageCommand | Records imutáveis como input dos services |
| **DTO Pattern** | Todos os endpoints | Nunca expor entity JPA direto |
| **Async Persistence** | Mensagens de chat | Não bloquear WebSocket esperando I/O |
| **Global Exception Handler** | @ControllerAdvice | Respostas de erro consistentes |
| **API Response Envelope** | ApiResponse<T> | Formato padronizado: `{ data, error, timestamp }` |

### ❌ Evitar (Anti-Patterns)

| Anti-Pattern | Problema | Alternativa |
|-------------|----------|-------------|
| **Anemic Domain Model** | Entidades sem lógica, tudo no service | Colocar regras de transição de status dentro de Ticket |
| **God Service** | TicketService com 2000 linhas | Separar em TicketService, AssignmentService, etc. |
| **N+1 Queries** | Listar tickets carregando agent um por um | `@EntityGraph` ou `JOIN FETCH` |
| **Expor Entity na API** | Mudança no banco quebra contrato da API | Sempre usar DTOs (Records) |
| **Salvar chat síncrono** | Bloqueia thread do WebSocket | `@Async` com thread pool dedicado |
| **Catch genérico** | `catch(Exception e)` esconde bugs | Exceptions específicas + GlobalHandler |
| **WebSocket sem auth** | Qualquer um conecta no chat | Validar JWT no handshake interceptor |
| **Polling ao invés de push** | Frontend fazendo GET a cada 2s | WebSocket para tudo que é realtime |

---

## 6. Decisões Técnicas Importantes

### Concorrência no Ticket Assignment

```java
// Ticket.java
@Version
private Integer version; // JPA gerencia automaticamente

// Ao tentar assign, se outro agente já pegou:
// → OptimisticLockException
// → Retorna 409 Conflict
// → Frontend mostra "ticket já atribuído"
```

### Persistência Async de Mensagens

```java
@Async("chatThreadPool")
public void persistMessage(Message message) {
    messageRepository.save(message);
}

// AsyncConfig.java
@Bean("chatThreadPool")
public Executor chatThreadPool() {
    var executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(500);
    return executor;
}
```

### Algoritmo Least Active Agent

```java
public Optional<AgentStatus> findBestAgent(UUID queueId) {
    return agentStatusRepository
        .findByQueueIdAndStatusAndActiveTicketsLessThanMaxTickets(queueId, "ONLINE")
        .stream()
        .min(Comparator.comparingInt(AgentStatus::getActiveTickets));
}
```

### WebSocket Auth

```java
// Interceptor no handshake
// Extrai JWT do query param: ws://host/ws?token=xxx
// Valida e injeta Principal na sessão
```

---

## 7. Escalabilidade Futura (pós-MVP)

| Fase | Componente | Substitui |
|------|-----------|-----------|
| V2 | Redis (cache + pub/sub) | Cache em memória, WebSocket single-instance |
| V3 | Kafka | ApplicationEventPublisher (para eventos entre serviços) |
| V4 | Multi-tenancy (schema per tenant) | Single tenant |
| V5 | Kubernetes | Docker Compose |

A arquitetura modular permite cada evolução sem reescrever o core.
