import { useMutation, useQueryClient } from '@tanstack/react-query'
import { transferToAgent, transferToQueue, type TransferPayload } from '@/api/transfers'
import { toast } from 'sonner'
import { getApiError } from '@/lib/error'

export function useTransferToAgent() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ ticketId, payload }: { ticketId: string; payload: TransferPayload }) =>
      transferToAgent(ticketId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tickets'] })
      queryClient.invalidateQueries({ queryKey: ['activities'] })
      toast.success('Ticket transferido para outro agente')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}

export function useTransferToQueue() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ ticketId, payload }: { ticketId: string; payload: TransferPayload }) =>
      transferToQueue(ticketId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tickets'] })
      queryClient.invalidateQueries({ queryKey: ['activities'] })
      toast.success('Ticket transferido para outra fila')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}
