import api from './client'
import type { ApiResponse, ChatSession, Message, Page } from '@/types'

export async function createChatSession(ticketId: string) {
  const { data } = await api.post<ApiResponse<ChatSession>>(`/chat/sessions?ticketId=${ticketId}`)
  return data.data
}

export async function getChatSessionByTicket(ticketId: string) {
  const { data } = await api.get<ApiResponse<ChatSession>>(`/chat/sessions/by-ticket/${ticketId}`)
  return data.data
}

export async function getChatSession(sessionId: string) {
  const { data } = await api.get<ApiResponse<ChatSession>>(`/chat/sessions/${sessionId}`)
  return data.data
}

export async function endChatSession(sessionId: string) {
  await api.post(`/chat/sessions/${sessionId}/end`)
}

export async function getMessages(sessionId: string, page = 0, size = 50) {
  const { data } = await api.get<ApiResponse<Page<Message>>>(
    `/chat/sessions/${sessionId}/messages?page=${page}&size=${size}`
  )
  return data.data
}

export async function sendMessageRest(sessionId: string, content: string) {
  const { data } = await api.post<ApiResponse<Message>>('/chat/messages', {
    sessionId,
    content,
  })
  return data.data
}