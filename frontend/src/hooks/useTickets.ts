import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/stores/authStore'
import {
  getTickets,
  getMyTickets,
  getTicketById,
  createTicket,
  closeTicket,
  type TicketFilters,
  type CreateTicketPayload,
} from '@/api/tickets'
import { toast } from 'sonner'
import { getApiError } from '@/lib/error'

export function useTicketList(filters: TicketFilters = {}) {
  const role = useAuthStore((s) => s.user?.role)

  return useQuery({
    queryKey: ['tickets', filters],
    queryFn: () => getTickets(filters),
    enabled: role === 'AGENT' || role === 'ADMIN',
    retry: false,
  })
}

export function useMyTickets(page = 0) {
  return useQuery({
    queryKey: ['tickets', 'my', page],
    queryFn: () => getMyTickets(page),
  })
}

export function useTicketDetail(id: string) {
  return useQuery({
    queryKey: ['tickets', id],
    queryFn: () => getTicketById(id),
    enabled: !!id,
  })
}

export function useCreateTicket() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CreateTicketPayload) => createTicket(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tickets'] })
      toast.success('Ticket criado com sucesso')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}

export function useCloseTicket() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (ticketId: string) => closeTicket(ticketId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tickets'] })
      toast.success('Ticket fechado')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}