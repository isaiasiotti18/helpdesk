import { useEffect, useRef, useState, useCallback } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/stores/authStore'
import {
  getChatSessionByTicket,
  createChatSession,
  getMessages,
  endChatSession,
} from '@/api/chat'
import {
  connectWebSocket,
  subscribeToSession,
  sendMessage as wsSendMessage,
} from '@/lib/websocket'
import type { Message } from '@/types'
import { getApiError } from '@/lib/error'
import { toast } from 'sonner'

export function useChatSession(ticketId: string) {
  return useQuery({
    queryKey: ['chat', 'session', ticketId],
    queryFn: () => getChatSessionByTicket(ticketId),
    retry: false,
  })
}

export function useCreateChatSession() {
  const queryClient = useQueryClient()

  return {
    create: async (ticketId: string) => {
      try {
        const session = await createChatSession(ticketId)
        queryClient.setQueryData(['chat', 'session', ticketId], session)
        toast.success('Chat iniciado')
        return session
      } catch (error) {
        toast.error(getApiError(error))
        throw error
      }
    },
  }
}

export function useEndChatSession() {
  const queryClient = useQueryClient()

  return {
    end: async (sessionId: string, ticketId: string) => {
      try {
        await endChatSession(sessionId)
        queryClient.invalidateQueries({ queryKey: ['chat', 'session', ticketId] })
        toast.info('Sessão encerrada')
      } catch (error) {
        toast.error(getApiError(error))
      }
    },
  }
}

export function useChatMessages(sessionId: string | undefined) {
  const token = useAuthStore((s) => s.accessToken)
  const [messages, setMessages] = useState<Message[]>([])
  const [connected, setConnected] = useState(false)
  const subscriptionRef = useRef<ReturnType<typeof subscribeToSession>>(null)

  // Carrega histórico via REST
  const historyQuery = useQuery({
    queryKey: ['chat', 'messages', sessionId],
    queryFn: () => getMessages(sessionId!),
    enabled: !!sessionId,
  })

  // Quando histórico carrega, seta como state inicial
  useEffect(() => {
    if (historyQuery.data?.content) {
      setMessages(historyQuery.data.content)
    }
  }, [historyQuery.data])

  // Conecta WebSocket e subscribe no tópico
  useEffect(() => {
    if (!sessionId || !token) return

    connectWebSocket(
      token,
      () => {
        setConnected(true)

        // O que faz: mensagem aparece instantaneamente com id temporário (temp-xxx). 
        // Quando o broadcast do WebSocket chega com o id real, substitui a otimista. 
        // Se o WebSocket falhar, a mensagem otimista fica na tela (mas não foi persistida).
        subscriptionRef.current = subscribeToSession(sessionId, (stompMessage) => {
          const newMessage: Message = JSON.parse(stompMessage.body)
          setMessages((prev) => {
            // Remove mensagem otimista com mesmo conteúdo se existir
            const withoutOptimistic = prev.filter(
              (m) => !m.id.startsWith('temp-') || m.content !== newMessage.content
            )
            // Evita duplicata
            if (withoutOptimistic.some((m) => m.id === newMessage.id)) return withoutOptimistic
            return [...withoutOptimistic, newMessage]
          })
        })
      },
      (error) => console.error('WebSocket error:', error)
    )

    return () => {
      subscriptionRef.current?.unsubscribe()
      subscriptionRef.current = null
    }
  }, [sessionId, token])

  const user = useAuthStore((s) => s.user)

  const sendMessage = useCallback(
    (content: string) => {
      if (!sessionId || !connected) return

      // Mensagem otimista
      const optimisticMessage: Message = {
        id: `temp-${Date.now()}`,
        sessionId,
        senderId: user?.id ?? '',
        senderName: user?.name ?? '',
        content,
        messageType: 'TEXT',
        sentAt: new Date().toISOString(),
      }

      setMessages((prev) => [...prev, optimisticMessage])
      wsSendMessage(sessionId, content)
    },
    [sessionId, connected, user]
  )

  return {
    messages,
    connected,
    isLoading: historyQuery.isLoading,
    sendMessage,
  }
}