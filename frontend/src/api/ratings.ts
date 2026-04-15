import api from './client'
import type { ApiResponse } from '@/types'

export interface Rating {
  id: string
  ticketId: string
  userName: string
  score: number
  comment: string | null
  createdAt: string
}

export async function rateTicket(ticketId: string, score: number, comment?: string) {
  const { data } = await api.post<ApiResponse<Rating>>(`/tickets/${ticketId}/rating`, {
    score,
    comment,
  })
  return data.data
}

export async function getTicketRating(ticketId: string) {
  const { data } = await api.get<ApiResponse<Rating>>(`/tickets/${ticketId}/rating`)
  return data.data
}
