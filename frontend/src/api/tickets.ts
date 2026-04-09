import api from './client'
import type { ApiResponse, Ticket, Page } from '@/types'

export interface CreateTicketPayload {
  title: string
  description?: string
  priority: string
}

export interface TicketFilters {
  status?: string
  priority?: string
  agentId?: string
  page?: number
  size?: number
}

export async function createTicket(payload: CreateTicketPayload) {
  const { data } = await api.post<ApiResponse<Ticket>>('/tickets', payload)
  return data.data
}

export async function getTickets(filters: TicketFilters = {}) {
  const params = new URLSearchParams()
  if (filters.status) params.set('status', filters.status)
  if (filters.priority) params.set('priority', filters.priority)
  if (filters.agentId) params.set('agentId', filters.agentId)
  params.set('page', String(filters.page ?? 0))
  params.set('size', String(filters.size ?? 20))

  const { data } = await api.get<ApiResponse<Page<Ticket>>>(`/tickets?${params}`)
  return data.data
}

export async function getMyTickets(page = 0, size = 20) {
  const { data } = await api.get<ApiResponse<Page<Ticket>>>(`/tickets/my?page=${page}&size=${size}`)
  return data.data
}

export async function getTicketById(id: string) {
  const { data } = await api.get<ApiResponse<Ticket>>(`/tickets/${id}`)
  return data.data
}

export async function assignTicket(ticketId: string, agentId: string) {
  const { data } = await api.post<ApiResponse<Ticket>>(`/tickets/${ticketId}/assign`, { agentId })
  return data.data
}

export async function updateTicketStatus(ticketId: string, status: string) {
  const { data } = await api.patch<ApiResponse<Ticket>>(`/tickets/${ticketId}/status`, { status })
  return data.data
}

export async function closeTicket(ticketId: string) {
  const { data } = await api.post<ApiResponse<Ticket>>(`/tickets/${ticketId}/close`)
  return data.data
}