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
  subscribeToNotes,
  subscribeToSession,
  sendMessage as wsSendMessage,
  sendNote as wsSendNote,
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

export function useChatMessages(sessionId: string | undefined, isAgent = false) {
  const token = useAuthStore((s) => s.accessToken)
  const user = useAuthStore((s) => s.user)
  const [messages, setMessages] = useState<Message[]>([])
  const [connected, setConnected] = useState(false)
  const subscriptionRef = useRef<ReturnType<typeof subscribeToSession>>(null)
  const notesSubRef = useRef<ReturnType<typeof subscribeToNotes>>(null)

  const historyQuery = useQuery({
    queryKey: ['chat', 'messages', sessionId, isAgent],
    queryFn: () => getMessages(sessionId!, 0, 50),
    enabled: !!sessionId,
  })

  useEffect(() => {
    if (historyQuery.data?.content) {
      setMessages(historyQuery.data.content)
    }
  }, [historyQuery.data])

  useEffect(() => {
    if (!sessionId || !token) return

    connectWebSocket(
      token,
      () => {
        setConnected(true)

        subscriptionRef.current = subscribeToSession(sessionId, (stompMessage) => {
          const newMessage: Message = JSON.parse(stompMessage.body)
          setMessages((prev) => {
            const withoutOptimistic = prev.filter(
              (m) => !m.id.startsWith('temp-') || m.content !== newMessage.content
            )
            if (withoutOptimistic.some((m) => m.id === newMessage.id)) return withoutOptimistic
            return [...withoutOptimistic, newMessage]
          })
        })

        // Agentes também recebem notas internas
        if (isAgent) {
          notesSubRef.current = subscribeToNotes(sessionId, (stompMessage) => {
            const note: Message = JSON.parse(stompMessage.body)
            setMessages((prev) => {
              if (prev.some((m) => m.id === note.id)) return prev
              return [...prev, note]
            })
          })
        }
      },
      (error) => console.error('WebSocket error:', error)
    )

    return () => {
      subscriptionRef.current?.unsubscribe()
      subscriptionRef.current = null
      notesSubRef.current?.unsubscribe()
      notesSubRef.current = null
    }
  }, [sessionId, token, isAgent])

  const sendMessage = useCallback(
    (content: string) => {
      if (!sessionId || !connected) return

      const optimisticMessage: Message = {
        id: `temp-${Date.now()}`,
        sessionId,
        senderId: user?.id ?? '',
        senderName: user?.name ?? '',
        content,
        messageType: 'TEXT',
        sentAt: new Date().toISOString(),
        isInternal: false,
      }

      setMessages((prev) => [...prev, optimisticMessage])
      wsSendMessage(sessionId, content)
    },
    [sessionId, connected, user]
  )

  const sendInternalNote = useCallback(
    (content: string) => {
      if (!sessionId || !connected) return

      const optimisticNote: Message = {
        id: `temp-note-${Date.now()}`,
        sessionId,
        senderId: user?.id ?? '',
        senderName: user?.name ?? '',
        content,
        messageType: 'NOTE',
        sentAt: new Date().toISOString(),
        isInternal: true,
      }

      setMessages((prev) => [...prev, optimisticNote])
      wsSendNote(sessionId, content)
    },
    [sessionId, connected, user]
  )

  return {
    messages,
    connected,
    isLoading: historyQuery.isLoading,
    sendMessage,
    sendInternalNote,
  }
}
