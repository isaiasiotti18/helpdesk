import { Badge } from '@/components/ui/badge'

interface SlaIndicatorProps {
  slaFirstResponseDeadline: string | null
  slaResolutionDeadline: string | null
  slaBbreached: boolean
  firstResponseAt: string | null
  status: string
}

export function SlaIndicator({
  slaFirstResponseDeadline,
  slaResolutionDeadline,
  slaBbreached,
  firstResponseAt,
  status,
}: SlaIndicatorProps) {
  if (!slaResolutionDeadline || status === 'CLOSED' || status === 'RESOLVED') return null

  if (slaBbreached) {
    return <Badge variant="destructive">SLA Estourado</Badge>
  }

  const now = new Date()
  const deadline = new Date(slaResolutionDeadline)
  const diffMs = deadline.getTime() - now.getTime()
  const diffMin = Math.floor(diffMs / 1000 / 60)

  // Primeira resposta pendente
  if (!firstResponseAt && slaFirstResponseDeadline) {
    const frDeadline = new Date(slaFirstResponseDeadline)
    const frDiffMin = Math.floor((frDeadline.getTime() - now.getTime()) / 1000 / 60)

    if (frDiffMin <= 0) {
      return <Badge variant="destructive">1ª Resposta atrasada</Badge>
    }
    if (frDiffMin <= 15) {
      return <Badge className="bg-amber-500 hover:bg-amber-600">1ª Resposta: {frDiffMin}min</Badge>
    }
  }

  // Resolução
  if (diffMin <= 0) {
    return <Badge variant="destructive">SLA Estourado</Badge>
  }

  if (diffMin <= 60) {
    return <Badge className="bg-amber-500 hover:bg-amber-600">SLA: {diffMin}min</Badge>
  }

  const diffHours = Math.floor(diffMin / 60)
  return <Badge variant="secondary">SLA: {diffHours}h</Badge>
}
