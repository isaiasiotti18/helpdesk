import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

let stompClient: Client | null = null

export function connectWebSocket(
  token: string,
  onConnect: () => void,
  onError?: (error: string) => void
) {
  if (stompClient?.active) return stompClient

  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect,
    onStompError: (frame) => {
      onError?.(frame.headers['message'] ?? 'WebSocket error')
    },
  })

  stompClient.activate()
  return stompClient
}

export function subscribeToSession(
  sessionId: string,
  callback: (message: IMessage) => void
) {
  if (!stompClient?.active) {
    console.warn('STOMP not connected')
    return null
  }

  return stompClient.subscribe(`/topic/chat.${sessionId}`, callback)
}

export function sendMessage(sessionId: string, content: string) {
  if (!stompClient?.active) {
    console.warn('STOMP not connected')
    return
  }

  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({ sessionId, content }),
  })
}

export function sendNote(sessionId: string, content: string) {
  if (!stompClient?.active) {
    console.warn('STOMP not connected')
    return
  }

  stompClient.publish({
    destination: '/app/chat.note',
    body: JSON.stringify({ sessionId, content }),
  })
}

export function subscribeToNotes(
  sessionId: string,
  callback: (message: IMessage) => void
) {
  if (!stompClient?.active) {
    console.warn('STOMP not connected')
    return null
  }

  return stompClient.subscribe(`/topic/chat.${sessionId}.notes`, callback)
}

export function disconnectWebSocket() {
  if (stompClient?.active) {
    stompClient.deactivate()
    stompClient = null
  }
}