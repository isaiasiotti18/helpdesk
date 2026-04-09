import { useAuthStore } from '@/stores/authStore'
import { useMyTickets, useTicketList } from '@/hooks/useTickets'
import { TicketCard } from '@/components/ticket/TicketCard'
import { QueryGuard } from '@/components/layout/QueryGuard'
import { useNavigate } from 'react-router-dom'
import { TicketListSkeleton } from '@/components/ticket/TicketSkeleton'
import { EmptyState } from '@/components/layout/EmptyState'

export function DashboardPage() {

  const navigate = useNavigate();

  const role = useAuthStore((s) => s.user?.role)
  const isAgent = role === 'AGENT' || role === 'ADMIN'

  const myTickets = useMyTickets()
  const openTickets = useTicketList({ status: 'OPEN' })

  return (
    <div className="p-6 space-y-8">
      <h1 className="text-2xl font-bold">Dashboard</h1>

      <section>
        <h2 className="text-lg font-semibold mb-4">Meus Tickets</h2>
        <QueryGuard
          query={myTickets}
          skeleton={<TicketListSkeleton count={3} />}
          emptyCheck={(data) => data.content.length === 0}
        >
          {(data) => (
            <div className="space-y-3">
              {data.content.map((ticket) => (
                <TicketCard key={ticket.id} ticket={ticket} />
              ))}
            </div>
          )}
        </QueryGuard>
      </section>

      {isAgent && (
        <section>
          <h2 className="text-lg font-semibold mb-4">Tickets Abertos</h2>
          {openTickets.isLoading && <p className="text-muted-foreground">Carregando...</p>}
          {openTickets.data?.content.length === 0 && (
            <EmptyState
              title="Nenhum ticket aberto"
              description="Todos os tickets foram resolvidos. Bom trabalho!"
            />
          )}
          <div className="space-y-3">
            {openTickets.data?.content.map((ticket) => (
              <TicketCard key={ticket.id} ticket={ticket} />
            ))}
          </div>
        </section>
      )}
    </div>
  )
}