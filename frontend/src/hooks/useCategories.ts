import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { getApiError } from '@/lib/error'
import {
  getActiveCategories,
  getAllCategories,
  createCategory,
  updateCategory,
  deleteCategory,
  type CreateCategoryPayload,
  type UpdateCategoryPayload,
} from '@/api/categories'

export function useActiveCategories() {
  return useQuery({
    queryKey: ['categories', 'active'],
    queryFn: getActiveCategories,
  })
}

export function useAllCategories() {
  return useQuery({
    queryKey: ['categories', 'all'],
    queryFn: getAllCategories,
  })
}

export function useCreateCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CreateCategoryPayload) => createCategory(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      toast.success('Categoria criada')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}

export function useUpdateCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateCategoryPayload }) =>
      updateCategory(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      toast.success('Categoria atualizada')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}

export function useDeleteCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => deleteCategory(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      toast.success('Categoria desativada')
    },
    onError: (error) => {
      toast.error(getApiError(error))
    },
  })
}
