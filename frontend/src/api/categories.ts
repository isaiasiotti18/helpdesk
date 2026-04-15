import api from './client'
import type { ApiResponse, Category } from '@/types'

export interface CreateCategoryPayload {
  name: string
  description?: string
  queueId?: string
}

export interface UpdateCategoryPayload {
  name?: string
  description?: string
  queueId?: string
  isActive?: boolean
}

export async function getActiveCategories() {
  const { data } = await api.get<ApiResponse<Category[]>>('/categories')
  return data.data
}

export async function getAllCategories() {
  const { data } = await api.get<ApiResponse<Category[]>>('/categories/all')
  return data.data
}

export async function getCategoryById(id: string) {
  const { data } = await api.get<ApiResponse<Category>>(`/categories/${id}`)
  return data.data
}

export async function createCategory(payload: CreateCategoryPayload) {
  const { data } = await api.post<ApiResponse<Category>>('/categories', payload)
  return data.data
}

export async function updateCategory(id: string, payload: UpdateCategoryPayload) {
  const { data } = await api.patch<ApiResponse<Category>>(`/categories/${id}`, payload)
  return data.data
}

export async function deleteCategory(id: string) {
  await api.delete(`/categories/${id}`)
}
