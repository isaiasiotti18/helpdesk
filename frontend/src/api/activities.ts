import api from './client'
import type { ApiResponse } from '@/types'

export interface Activity {
  id: string
  ticketId: string
  userId: string | null
  userName: string
  action: string
  detail: string | null
  createdAt: string
}

export async function getTicketActivities(ticketId: string) {
  const { data } = await api.get<ApiResponse<Activity[]>>(`/tickets/${ticketId}/activities`)
  return data.data
}
