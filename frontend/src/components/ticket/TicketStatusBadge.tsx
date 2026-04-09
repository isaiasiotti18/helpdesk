import { Badge } from '@/components/ui/badge'
import { STATUS_LABELS, STATUS_VARIANTS, PRIORITY_LABELS, PRIORITY_VARIANTS } from '@/lib/constants'
import type { TicketStatus, Priority } from '@/types'

export function TicketStatusBadge({ status }: { status: TicketStatus }) {
  return <Badge variant={STATUS_VARIANTS[status]}>{STATUS_LABELS[status]}</Badge>
}

export function PriorityBadge({ priority }: { priority: Priority }) {
  return <Badge variant={PRIORITY_VARIANTS[priority]}>{PRIORITY_LABELS[priority]}</Badge>
}