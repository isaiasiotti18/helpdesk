import api from './client'
import type { ApiResponse } from '@/types'

export interface SearchResult {
  id: string
  title: string
  description: string | null
  status: string
  priority: string
  createdByName: string
  assignedAgentName: string | null
  categoryName: string | null
  createdAt: string
}

export async function searchTickets(query: string, limit = 20) {
  const { data } = await api.get<ApiResponse<SearchResult[]>>(
    `/search?q=${encodeURIComponent(query)}&limit=${limit}`
  )
  return data.data
}
