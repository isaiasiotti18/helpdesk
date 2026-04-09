import type { TicketStatus, Priority } from '@/types'

export const STATUS_LABELS: Record<TicketStatus, string> = {
    OPEN: 'Aberto',
    IN_QUEUE: 'Na fila',
    IN_PROGRESS: 'Em atendimento',
    TRANSFERRED: 'Transferido',
    RESOLVED: 'Resolvido',
    CLOSED: 'Fechado',
}

export const PRIORITY_LABELS: Record<Priority, string> = {
    LOW: 'Baixa',
    MEDIUM: 'Média',
    HIGH: 'Alta',
    URGENT: 'Urgente',
}

export const STATUS_VARIANTS: Record<TicketStatus, 'default' | 'secondary' | 'destructive' | 'outline'> = {
    OPEN: 'default',
    IN_QUEUE: 'secondary',
    IN_PROGRESS: 'default',
    TRANSFERRED: 'outline',
    RESOLVED: 'secondary',
    CLOSED: 'outline',
}

export const PRIORITY_VARIANTS: Record<Priority, 'default' | 'secondary' | 'destructive' | 'outline'> = {
    LOW: 'outline',
    MEDIUM: 'secondary',
    HIGH: 'default',
    URGENT: 'destructive',
}

export const ALL_STATUSES: TicketStatus[] = ['OPEN', 'IN_QUEUE', 'IN_PROGRESS', 'TRANSFERRED', 'RESOLVED', 'CLOSED']
export const ALL_PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']