import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { rateTicket, getTicketRating } from '@/api/ratings'
import { toast } from 'sonner'
import { getApiError } from '@/lib/error'

export function useTicketRating(ticketId: string) {
  return useQuery({
    queryKey: ['rating', ticketId],
    queryFn: () => getTicketRating(ticketId),
    retry: false, // 404 é esperado quando não tem rating
  })
}

export function useRateTicket() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ ticketId, score, comment }: { ticketId: string; score: number; comment?: string }) =>
      rateTicket(ticketId, score, comment),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['rating', variables.ticketId] })
      toast.success('Avaliação enviada! Obrigado.')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}
