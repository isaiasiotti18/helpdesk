import { useTicketActivities } from '@/hooks/useActivities'
import { relativeDate } from '@/lib/date'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import type { Activity } from '@/api/activities'

const ACTION_LABELS: Record<string, string> = {
  CREATED: 'Ticket criado',
  ASSIGNED: 'Atribuído a agente',
  TRANSFERRED: 'Transferido',
  STATUS_CHANGED: 'Status alterado',
  PRIORITY_CHANGED: 'Prioridade alterada',
  CLOSED: 'Ticket fechado',
  RESOLVED: 'Ticket resolvido',
  NOTE_ADDED: 'Nota interna adicionada',
  MESSAGE_SENT: 'Mensagem enviada',
  CATEGORY_CHANGED: 'Categoria alterada',
  QUEUE_CHANGED: 'Fila alterada',
}

const ACTION_COLORS: Record<string, string> = {
  CREATED: 'bg-blue-500',
  ASSIGNED: 'bg-green-500',
  TRANSFERRED: 'bg-amber-500',
  STATUS_CHANGED: 'bg-purple-500',
  CLOSED: 'bg-gray-500',
  RESOLVED: 'bg-emerald-500',
  NOTE_ADDED: 'bg-amber-400',
  MESSAGE_SENT: 'bg-blue-400',
}

function parseDetail(detail: string | null): Record<string, string> | null {
  if (!detail) return null
  try {
    return JSON.parse(detail)
  } catch {
    return null
  }
}

function ActivityItem({ activity }: { activity: Activity }) {
  const detail = parseDetail(activity.detail)
  const color = ACTION_COLORS[activity.action] ?? 'bg-gray-400'
  const label = ACTION_LABELS[activity.action] ?? activity.action

  return (
    <div className="flex gap-3">
      <div className="flex flex-col items-center">
        <div className={`w-3 h-3 rounded-full ${color} mt-1.5 shrink-0`} />
        <div className="w-px flex-1 bg-border" />
      </div>
      <div className="pb-6">
        <p className="text-sm font-medium">{label}</p>
        <p className="text-xs text-muted-foreground">
          {activity.userName} · {relativeDate(activity.createdAt)}
        </p>
        {detail && (
          <div className="mt-1 text-xs text-muted-foreground space-y-0.5">
            {detail.from && detail.to && (
              <p>{detail.from} → {detail.to}</p>
            )}
            {detail.agent && <p>Agente: {detail.agent}</p>}
            {detail.queue && <p>Fila: {detail.queue}</p>}
            {detail.category && <p>Categoria: {detail.category}</p>}
          </div>
        )}
      </div>
    </div>
  )
}

interface TicketTimelineProps {
  ticketId: string
}

export function TicketTimeline({ ticketId }: TicketTimelineProps) {
  const { data: activities, isLoading } = useTicketActivities(ticketId)

  if (isLoading) return <LoadingSpinner />

  if (!activities || activities.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma atividade registrada.</p>
  }

  return (
    <div className="space-y-0">
      {activities.map((activity) => (
        <ActivityItem key={activity.id} activity={activity} />
      ))}
    </div>
  )
}
