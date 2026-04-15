export interface User {
  id: string
  name: string
  email: string
  role: 'CLIENT' | 'AGENT' | 'ADMIN'
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface ApiResponse<T> {
  data: T
  error: string | null
  timestamp: string
}

export interface Ticket {
  id: string
  title: string
  description: string
  status: TicketStatus
  priority: Priority
  createdById: string
  createdByName: string
  assignedAgentName: string | null
  createdAt: string
  closedAt: string | null
  categoryId: string | null
  categoryName: string | null
}

export type TicketStatus = 'OPEN' | 'IN_QUEUE' | 'IN_PROGRESS' | 'TRANSFERRED' | 'RESOLVED' | 'CLOSED'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

export interface ChatSession {
  id: string
  ticketId: string
  startedAt: string
  endedAt: string | null
  active: boolean
}

export interface Message {
  id: string
  sessionId: string
  senderId: string
  senderName: string
  content: string
  messageType: 'TEXT' | 'SYSTEM' | 'FILE'
  sentAt: string
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface Category {
  id: string
  name: string
  description: string | null
  queueId: string | null
  queueName: string | null
  isActive: boolean
  createdAt: string
}
