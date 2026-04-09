import { formatDistanceToNow, format, isToday, isYesterday } from 'date-fns'
import { ptBR } from 'date-fns/locale'

export function relativeDate(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMin = diffMs / 1000 / 60

  // Menos de 1 minuto
  if (diffMin < 1) return 'agora'

  // Menos de 1 hora
  if (diffMin < 60) return formatDistanceToNow(date, { addSuffix: true, locale: ptBR })

  // Hoje
  if (isToday(date)) return `hoje às ${format(date, 'HH:mm')}`

  // Ontem
  if (isYesterday(date)) return `ontem às ${format(date, 'HH:mm')}`

  // Até 7 dias
  if (diffMin < 60 * 24 * 7) return formatDistanceToNow(date, { addSuffix: true, locale: ptBR })

  // Mais antigo
  return format(date, "dd/MM/yyyy 'às' HH:mm")
}

export function chatTime(dateStr: string): string {
  const date = new Date(dateStr)

  if (isToday(date)) return format(date, 'HH:mm')
  if (isYesterday(date)) return `ontem ${format(date, 'HH:mm')}`

  return format(date, 'dd/MM HH:mm')
}