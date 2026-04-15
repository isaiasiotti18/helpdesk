import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { useAuthStore } from '@/stores/authStore'
import { connectWebSocket } from '@/lib/websocket'
import { Client } from '@stomp/stompjs'

interface NotificationPayload {
  id: string
  type: string
  title: string
  content: string | null
  ticketId: string | null
  isRead: boolean
  createdAt: string
}

declare global {
  interface Window {
    __stompClient?: Client | null
  }
}

export function useNotificationSocket() {
  const queryClient = useQueryClient()
  const user = useAuthStore((s) => s.user)
  const accessToken = useAuthStore((s) => s.accessToken)

  useEffect(() => {
    if (!user?.id || !accessToken) return

    let sub: { unsubscribe: () => void } | null = null

    const client = connectWebSocket(accessToken, () => {
      sub = client.subscribe(`/topic/notifications.${user.id}`, (msg) => {
        try {
          const payload: NotificationPayload = JSON.parse(msg.body)
          queryClient.invalidateQueries({ queryKey: ['notifications'] })
          toast.info(payload.title, {
            description: payload.content ?? undefined,
          })
        } catch {
          // ignora payloads inválidos
        }
      })
    })

    return () => {
      sub?.unsubscribe()
    }
  }, [user?.id, accessToken, queryClient])
}
