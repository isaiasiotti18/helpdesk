import { useNavigate } from 'react-router-dom'
import { Card, CardContent } from '@/components/ui/card'
import { TicketStatusBadge, PriorityBadge } from './TicketStatusBadge'
import type { Ticket } from '@/types'
import { relativeDate } from '@/lib/date'

interface TicketCardProps {
  ticket: Ticket
}

export function TicketCard({ ticket }: TicketCardProps) {
  const navigate = useNavigate()

  return (
    <Card
      className="cursor-pointer hover:shadow-md transition-shadow"
      onClick={() => navigate(`/tickets/${ticket.id}`)}
    >
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <h3 className="font-medium truncate">{ticket.title}</h3>
            <p className="text-sm text-muted-foreground mt-1">
              {ticket.createdByName}
              {ticket.assignedAgentName && ` → ${ticket.assignedAgentName}`}
            </p>
          </div>
          <div className="flex gap-2 shrink-0">
            <PriorityBadge priority={ticket.priority} />
            <TicketStatusBadge status={ticket.status} />
          </div>
        </div>
        <p className="text-xs text-muted-foreground mt-2">
          {relativeDate(ticket.createdAt)}
        </p>
      </CardContent>
    </Card>
  )
}