import { useState } from 'react'
import { useAuthStore } from '@/stores/authStore'
import { useMyTickets, useTicketList } from '@/hooks/useTickets'
import { TicketCard } from '@/components/ticket/TicketCard'
import { Button } from '@/components/ui/button'
import { useNavigate } from 'react-router-dom'
import { ALL_STATUSES, STATUS_LABELS } from '@/lib/constants'
import { EmptyState } from '@/components/layout/EmptyState'

export function TicketsPage() {
  const navigate = useNavigate()
  const role = useAuthStore((s) => s.user?.role)
  const isAgent = role === 'AGENT' || role === 'ADMIN'

  const [statusFilter, setStatusFilter] = useState('')
  const [page, setPage] = useState(0)

  const agentQuery = useTicketList({ status: statusFilter || undefined, page })
  const clientQuery = useMyTickets(page)

  const query = isAgent ? agentQuery : clientQuery
  const tickets = query.data

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Tickets</h1>
        <Button onClick={() => navigate('/tickets/new')}>Novo Ticket</Button>
      </div>

      {isAgent && (
        <div className="flex gap-2 flex-wrap">
          {ALL_STATUSES.map((s) => (
            <Button
              key={s}
              variant={statusFilter === s ? 'default' : 'outline'}
              size="sm"
              onClick={() => { setStatusFilter(s); setPage(0) }}
            >
              {STATUS_LABELS[s]}
            </Button>
          ))}
        </div>
      )}

      {query.isLoading && <p className="text-muted-foreground">Carregando...</p>}

      {tickets?.content.length === 0 && !query.isLoading && (
        <EmptyState
          title="Nenhum resultado"
          description="Tente ajustar os filtros para encontrar o que procura."
        />
      )}

      <div className="space-y-3">
        {tickets?.content.map((ticket) => (
          <TicketCard key={ticket.id} ticket={ticket} />
        ))}
      </div>

      {tickets && tickets.totalPages > 1 && (
        <div className="flex gap-2 justify-center">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            Anterior
          </Button>
          <span className="text-sm self-center text-muted-foreground">
            {page + 1} de {tickets.totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page + 1 >= tickets.totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            Próxima
          </Button>
        </div>
      )}
    </div>
  )
}