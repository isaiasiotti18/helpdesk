import api from './client'
import type { ApiResponse, Ticket } from '@/types'

export interface TransferPayload {
  agentId?: string
  queueId?: string
  reason?: string
}

export async function transferToAgent(ticketId: string, payload: TransferPayload) {
  const { data } = await api.post<ApiResponse<Ticket>>(`/tickets/${ticketId}/transfer-agent`, payload)
  return data.data
}

export async function transferToQueue(ticketId: string, payload: TransferPayload) {
  const { data } = await api.post<ApiResponse<Ticket>>(`/tickets/${ticketId}/transfer-queue`, payload)
  return data.data
}
