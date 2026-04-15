import api from './client'
import type { ApiResponse, Page } from '@/types'

export interface NotificationItem {
  id: string
  type: string
  title: string
  content: string | null
  ticketId: string | null
  isRead: boolean
  createdAt: string
}

export async function getNotifications(page = 0) {
  const { data } = await api.get<ApiResponse<Page<NotificationItem>>>(`/notifications?page=${page}&size=20`)
  return data.data
}

export async function getUnreadCount() {
  const { data } = await api.get<ApiResponse<number>>('/notifications/unread-count')
  return data.data
}

export async function markAsRead(id: string) {
  await api.post(`/notifications/${id}/read`)
}

export async function markAllAsRead() {
  await api.post('/notifications/read-all')
}
