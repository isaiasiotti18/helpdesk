import { useParams, useNavigate } from 'react-router-dom'
import { useChatSession, useCreateChatSession, useEndChatSession } from '@/hooks/useChat'
import { useTicketDetail } from '@/hooks/useTickets'
import { ChatWindow } from '@/components/chat/ChatWindow'
import { TicketStatusBadge, PriorityBadge } from '@/components/ticket/TicketStatusBadge'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { getApiError } from '@/lib/error'
import { toast } from 'sonner'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'

export function ChatPage() {
  const { ticketId } = useParams<{ ticketId: string }>()
  const navigate = useNavigate()

  const { data: ticket, isLoading: ticketLoading } = useTicketDetail(ticketId!)
  const { data: session, isLoading: sessionLoading, error: sessionError } = useChatSession(ticketId!)
  const { create } = useCreateChatSession()
  const { end } = useEndChatSession()

  if (ticketLoading || sessionLoading) {
    return <div className="p-6 text-muted-foreground">Carregando...</div>
  }

  if (!ticket) {
    return <div className="p-6 text-muted-foreground">Ticket não encontrado.</div>
  }

  const hasSession = session && !sessionError
  const canCreateSession = ticket.status === 'IN_PROGRESS' && !hasSession
  const canEndSession = hasSession && session.active

  async function handleCreateSession() {
    try {
      await create(ticketId!)
    } catch (error) {
      toast.error(getApiError(error))
    }
  }

  async function handleEndSession() {
    if (session) {
      await end(session.id, ticketId!)
    }
  }

  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center justify-between p-4 border-b bg-card">
        <div className="flex items-center gap-3">
          <Button variant="outline" size="sm" onClick={() => navigate(`/tickets/${ticketId}`)}>
            ← Ticket
          </Button>
          <div>
            <h2 className="font-medium">{ticket.title}</h2>
            <p className="text-xs text-muted-foreground">
              {ticket.assignedAgentName ?? 'Sem agente'}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <PriorityBadge priority={ticket.priority} />
          <TicketStatusBadge status={ticket.status} />
          {canEndSession && (
            <ConfirmDialog
              trigger={
                <Button variant="destructive" size="sm">
                  Encerrar Chat
                </Button>
              }
              title="Encerrar sessão de chat?"
              description="As mensagens serão mantidas, mas não será possível enviar novas mensagens."
              confirmLabel="Encerrar"
              onConfirm={handleEndSession}
            />
          )}
        </div>
      </div>

      {hasSession ? (
        <ChatWindow sessionId={session.id} active={session.active} />
      ) : canCreateSession ? (
        <div className="flex-1 flex items-center justify-center">
          <Card className="p-8 text-center space-y-4">
            <p className="text-muted-foreground">
              Nenhuma sessão de chat ativa para este ticket.
            </p>
            <Button onClick={handleCreateSession}>Iniciar Chat</Button>
          </Card>
        </div>
      ) : (
        <div className="flex-1 flex items-center justify-center">
          <p className="text-muted-foreground">
            O ticket precisa estar em atendimento para iniciar o chat.
          </p>
        </div>
      )}
    </div>
  )
}