# DESCRIPTION.md

## O que é

Sistema de helpdesk com gestão de chamados e chat em tempo real. Clientes abrem tickets, entram em fila de atendimento, e conversam com agentes ao vivo. Agentes recebem tickets automaticamente baseado em carga de trabalho, podem transferir atendimentos e encerrar chamados.

## Para quem

Empresas pequenas e médias que precisam de:
- Central de atendimento ao cliente
- Suporte interno (TI, RH, facilities)
- Atendimento de emergência/urgência com resposta rápida

## Problema que resolve

Ferramentas como Zendesk e Intercom são caras e complexas para operações menores. Planilhas e WhatsApp não escalam e não geram métricas. Este sistema ocupa o espaço entre os dois: funcional, simples, e com dados acionáveis.

## Funcionalidades principais

**Para clientes:**
- Abrir chamado com título, descrição e prioridade
- Acompanhar status do chamado em tempo real
- Chat ao vivo com agente designado
- Histórico de todos os atendimentos

**Para agentes:**
- Receber tickets automaticamente (fila inteligente)
- Responder via chat em tempo real
- Transferir atendimento para outro agente
- Encerrar e resolver chamados

**Para admins:**
- Gerenciar filas e agentes
- Dashboard com métricas: tempo médio de resposta, tickets/hora, satisfação
- Monitorar agentes online e carga de trabalho

## Diferencial técnico

- **Atribuição inteligente**: algoritmo Least Active Agent distribui tickets para o agente com menor carga, respeitando limites configuráveis.
- **Realtime nativo**: chat via WebSocket, não polling. Entrega instantânea.
- **Concorrência tratada**: optimistic locking impede dois agentes pegarem o mesmo ticket.
- **Arquitetura evolutiva**: modular monolith preparado para escalar horizontalmente.
