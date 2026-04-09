import { useEffect, useRef } from 'react'
import { useChatMessages } from '@/hooks/useChat'
import { MessageBubble } from './MessageBubble'
import { MessageInput } from './MessageInput'
import { ChatSkeleton } from './ChatSkeleton'
import { EmptyState } from '@/components/layout/EmptyState'
import { ScrollArea } from '@/components/ui/scroll-area'

interface ChatWindowProps {
  sessionId: string
  active: boolean
}

export function ChatWindow({ sessionId, active }: ChatWindowProps) {
  const { messages, connected, isLoading, sendMessage } = useChatMessages(sessionId)
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center gap-2 p-4 border-b">
        <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500' : 'bg-red-500'}`} />
        <span className="text-sm text-muted-foreground">
          {connected ? 'Conectado' : 'Conectando...'}
        </span>
      </div>

      <ScrollArea className="flex-1 p-4">
        {isLoading && <ChatSkeleton />}

        {!isLoading && messages.length === 0 && (
          <EmptyState
            title="Nenhuma mensagem"
            description="Envie a primeira mensagem para iniciar a conversa."
          />
        )}

        <div className="space-y-3">
          {messages.map((msg) => (
            <MessageBubble key={msg.id} message={msg} />
          ))}
          <div ref={bottomRef} />
        </div>
      </ScrollArea>

      <MessageInput
        onSend={sendMessage}
        disabled={!active || !connected}
      />
    </div>
  )
}