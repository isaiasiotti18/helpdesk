import api from './client'
import type { ApiResponse } from '@/types'

export interface DashboardMetrics {
  ticketCounts: {
    open: number
    inProgress: number
    resolved: number
    closed: number
    slaBbreached: number
    total: number
  }
  performance: {
    avgFirstResponseMinutes: number | null
    avgResolutionMinutes: number | null
    slaCompliancePercent: number | null
  }
  agentLoad: Array<{
    agentId: string
    agentName: string
    activeTickets: number
    resolvedToday: number
  }>
  ticketsPerDay: Array<{
    date: string
    count: number
  }>
}

export async function getDashboardMetrics() {
  const { data } = await api.get<ApiResponse<DashboardMetrics>>('/metrics/dashboard')
  return data.data
}
