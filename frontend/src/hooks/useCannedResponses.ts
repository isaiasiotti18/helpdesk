import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  getCannedResponses,
  searchCannedResponses,
  createCannedResponse,
  deleteCannedResponse,
  type CreateCannedPayload,
} from '@/api/canned'
import { toast } from 'sonner'
import { getApiError } from '@/lib/error'

export function useCannedResponses() {
  return useQuery({
    queryKey: ['canned-responses'],
    queryFn: getCannedResponses,
  })
}

export function useSearchCanned(query: string) {
  return useQuery({
    queryKey: ['canned-responses', 'search', query],
    queryFn: () => searchCannedResponses(query),
    enabled: query.length >= 1,
  })
}

export function useCreateCanned() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CreateCannedPayload) => createCannedResponse(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['canned-responses'] })
      toast.success('Resposta pronta criada')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}

export function useDeleteCanned() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => deleteCannedResponse(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['canned-responses'] })
      toast.success('Resposta pronta removida')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}
