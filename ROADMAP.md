# ROADMAP.md

## Concluído

### Segurança ✅
- [x] Handler de AuthorizationDeniedException (JSON ao invés de HTML)
- [x] Validação de ownership nos tickets (CLIENT só vê os seus)
- [x] Refresh token no banco (SHA-256, rotação, revogação)
- [x] Rate limiting no login (5 tentativas / 5 min)
- [x] CORS configurável
- [x] Validação do JWT secret no startup
- [x] Validação de role no assign

### Arquitetura Backend ✅
- [x] N+1 queries resolvido com @EntityGraph
- [x] JPA Specifications para filtros composáveis
- [x] Paginação cursor-based nas mensagens
- [x] Audit log assíncrono
- [x] Soft delete (tickets e users)
- [x] Entidade TicketAssignment (histórico)
- [x] Batch insert de mensagens
- [x] Connection pool tuning (Neon)

### Frontend Arquitetura ✅
- [x] Error boundaries (global + por rota)
- [x] Loading states consistentes (QueryGuard + Skeleton)
- [x] Interceptor de refresh token com fila
- [x] Toast notifications (Sonner)
- [x] Optimistic updates no chat
- [x] Helper getApiError()
- [x] Constants centralizadas
- [x] React Query devtools
- [x] FormField reutilizável

### Frontend UX ✅
- [x] Responsive (sidebar mobile com Sheet)
- [x] Skeleton loading (tickets + chat)
- [x] Empty states contextualizados
- [x] Confirmação em ações destrutivas
- [x] Datas relativas (date-fns pt-BR)

### Features Core ✅
- [x] Categorias e Departamentos (auto-routing)
- [x] Notas Internas (canal WebSocket separado)
- [x] Timeline de Atividades
- [x] Respostas Prontas (picker com /)
- [x] Transferência entre Filas/Agentes

### Features Avançadas ✅
- [x] SLA (policies, deadlines, breach check)
- [x] Notificações (push WebSocket + persistência)
- [x] Dashboard de Métricas (KPIs, agent load, chart)
- [x] CSAT (avaliação 1-5 estrelas)
- [x] Busca Full-Text

### Infra ✅
- [x] Docker multi-stage (backend + frontend)
- [x] Docker Compose (full stack + dev)
- [x] Nginx (SPA fallback, proxy API/WS, gzip, cache)
- [x] GitHub Actions CI (testes, type check, build, docker)

---

## Futuro (V2+)

### Features
- [ ] Tags nos tickets
- [ ] Anexos (upload de arquivos)
- [ ] Knowledge Base (artigos de ajuda)
- [ ] Portal do Cliente (self-service)
- [ ] Multi-canal (WhatsApp, Email)
- [ ] Automações (regras: "se prioridade = URGENT, notificar Slack")
- [ ] Webhooks
- [ ] Relatórios exportáveis (PDF/Excel)
- [ ] Integração IA (sugestão de resposta, classificação automática)

### Infra
- [ ] Redis (cache + pub/sub para WebSocket multi-instance)
- [ ] Multi-tenancy (schema per tenant)
- [ ] CDN para assets
- [ ] Kubernetes
- [ ] Observabilidade (Prometheus + Grafana + Loki)
- [ ] Deploy em cloud (AWS/GCP/Fly.io)

### Qualidade
- [ ] Corrigir testes de integração
- [ ] Aumentar cobertura de testes (>80%)
- [ ] Testes E2E (Playwright)
- [ ] Testes de carga (k6)
