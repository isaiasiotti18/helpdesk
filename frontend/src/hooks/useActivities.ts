import { useQuery } from '@tanstack/react-query'
import { getTicketActivities } from '@/api/activities'

export function useTicketActivities(ticketId: string) {
  return useQuery({
    queryKey: ['activities', ticketId],
    queryFn: () => getTicketActivities(ticketId),
    enabled: !!ticketId,
  })
}
