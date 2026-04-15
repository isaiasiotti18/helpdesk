import api from './client'
import type { ApiResponse } from '@/types'

export interface CannedResponse {
  id: string
  title: string
  content: string
  shortcut: string | null
  category: string | null
  createdByName: string
  isShared: boolean
  createdAt: string
}

export interface CreateCannedPayload {
  title: string
  content: string
  shortcut?: string
  category?: string
  isShared?: boolean
}

export async function getCannedResponses() {
  const { data } = await api.get<ApiResponse<CannedResponse[]>>('/canned-responses')
  return data.data
}

export async function searchCannedResponses(query: string) {
  const { data } = await api.get<ApiResponse<CannedResponse[]>>(`/canned-responses/search?q=${query}`)
  return data.data
}

export async function createCannedResponse(payload: CreateCannedPayload) {
  const { data } = await api.post<ApiResponse<CannedResponse>>('/canned-responses', payload)
  return data.data
}

export async function updateCannedResponse(id: string, payload: Partial<CreateCannedPayload>) {
  const { data } = await api.put<ApiResponse<CannedResponse>>(`/canned-responses/${id}`, payload)
  return data.data
}

export async function deleteCannedResponse(id: string) {
  await api.delete(`/canned-responses/${id}`)
}
