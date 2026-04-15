import { useParams, useNavigate } from 'react-router-dom'
import { useTicketDetail, useCloseTicket } from '@/hooks/useTickets'
import { TicketStatusBadge, PriorityBadge } from '@/components/ticket/TicketStatusBadge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { TicketTimeline } from '@/components/ticket/TicketTimeline'
import { TransferDialog } from '@/components/ticket/TransferDialog'
import { SlaIndicator } from '@/components/ticket/SlaIndicator'
import { RatingWidget } from '@/components/ticket/RatingWidget'
import { useAuthStore } from '@/stores/authStore'
import { relativeDate } from '@/lib/date'

export function TicketDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: ticket, isLoading } = useTicketDetail(id!)
  const closeTicketMutation = useCloseTicket()
  const user = useAuthStore((s) => s.user)
  const isAgent = user?.role === 'AGENT' || user?.role === 'ADMIN'

  if (isLoading) return <div className="p-6 text-muted-foreground">Carregando...</div>
  if (!ticket) return <div className="p-6 text-muted-foreground">Ticket não encontrado.</div>

  const canClose = ticket.status !== 'CLOSED' && ticket.status !== 'RESOLVED'
  const isClosed = ticket.status === 'CLOSED' || ticket.status === 'RESOLVED'
  const isCreator = ticket.createdById === user?.id

  async function handleClose() {
    await closeTicketMutation.mutateAsync(ticket!.id)
  }

  return (
    <div className="p-6 max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <Button variant="outline" size="sm" onClick={() => navigate('/tickets')}>
          ← Voltar
        </Button>
        <div className="flex gap-2 flex-wrap items-center justify-end">
          <PriorityBadge priority={ticket.priority} />
          <TicketStatusBadge status={ticket.status} />
          <SlaIndicator
            slaFirstResponseDeadline={ticket.slaFirstResponseDeadline}
            slaResolutionDeadline={ticket.slaResolutionDeadline}
            slaBbreached={ticket.slaBbreached}
            firstResponseAt={ticket.firstResponseAt}
            status={ticket.status}
          />
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{ticket.title}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {ticket.description && (
            <p className="text-sm text-muted-foreground">{ticket.description}</p>
          )}

          <Separator />

          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-muted-foreground">Criado por</p>
              <p className="font-medium">{ticket.createdByName}</p>
            </div>
            <div>
              <p className="text-muted-foreground">Agente</p>
              <p className="font-medium">{ticket.assignedAgentName ?? 'Não atribuído'}</p>
            </div>
            {ticket.categoryName && (
              <div>
                <p className="text-muted-foreground">Categoria</p>
                <p className="font-medium">{ticket.categoryName}</p>
              </div>
            )}
            <div>
              <p className="text-muted-foreground">Criado em</p>
              <p className="font-medium">{relativeDate(ticket.createdAt)}</p>
            </div>
            {ticket.closedAt && (
              <div>
                <p className="text-muted-foreground">Fechado em</p>
                <p className="font-medium">
                  {relativeDate(ticket.closedAt)}
                </p>
              </div>
            )}
          </div>

          {canClose && (
            <>
              <Separator />
              <div className="flex gap-3">
                <ConfirmDialog
                  trigger={
                    <Button variant="destructive" disabled={closeTicketMutation.isPending}>
                      {closeTicketMutation.isPending ? 'Fechando...' : 'Fechar Ticket'}
                    </Button>
                  }
                  title="Fechar ticket?"
                  description="Esta ação não pode ser desfeita. O ticket será marcado como fechado."
                  confirmLabel="Fechar"
                  onConfirm={handleClose}
                />
                {ticket.status === 'IN_PROGRESS' && (
                  <Button onClick={() => navigate(`/chat/${ticket.id}`)}>
                    Abrir Chat
                  </Button>
                )}
                {ticket.status === 'IN_PROGRESS' && isAgent && (
                  <TransferDialog
                    ticketId={ticket.id}
                    queues={[]}
                    agents={[]}
                  />
                )}
              </div>
            </>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-6 space-y-4">
          <h3 className="text-lg font-semibold">Atividades</h3>
          <Separator />
          <TicketTimeline ticketId={ticket.id} />
        </CardContent>
      </Card>

      {isClosed && <RatingWidget ticketId={ticket.id} canRate={isCreator} />}
    </div>
  )
}