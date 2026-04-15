import { useState, useEffect, useRef } from 'react'
import { useChatMessages } from '@/hooks/useChat'
import { useAuthStore } from '@/stores/authStore'
import { MessageBubble } from './MessageBubble'
import { MessageInput } from './MessageInput'
import { NoteInput } from './NoteInput'
import { ChatSkeleton } from './ChatSkeleton'
import { EmptyState } from '@/components/layout/EmptyState'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Button } from '@/components/ui/button'

interface ChatWindowProps {
  sessionId: string
  active: boolean
}

export function ChatWindow({ sessionId, active }: ChatWindowProps) {
  const role = useAuthStore((s) => s.user?.role)
  const isAgent = role === 'AGENT' || role === 'ADMIN'
  const [tab, setTab] = useState<'chat' | 'notes'>('chat')

  const { messages, connected, isLoading, sendMessage, sendInternalNote } =
    useChatMessages(sessionId, isAgent)

  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const filteredMessages =
    tab === 'notes'
      ? messages.filter((m) => m.isInternal)
      : messages.filter((m) => !m.isInternal)

  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center justify-between p-4 border-b">
        <div className="flex items-center gap-2">
          <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500' : 'bg-red-500'}`} />
          <span className="text-sm text-muted-foreground">
            {connected ? 'Conectado' : 'Conectando...'}
          </span>
        </div>

        {isAgent && (
          <div className="flex gap-1">
            <Button
              variant={tab === 'chat' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setTab('chat')}
            >
              Chat
            </Button>
            <Button
              variant={tab === 'notes' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setTab('notes')}
              className={tab === 'notes' ? 'bg-amber-500 hover:bg-amber-600' : ''}
            >
              Notas
            </Button>
          </div>
        )}
      </div>

      <ScrollArea className="flex-1 p-4">
        {isLoading && <ChatSkeleton />}

        {!isLoading && filteredMessages.length === 0 && (
          <EmptyState
            title={tab === 'notes' ? 'Nenhuma nota interna' : 'Nenhuma mensagem'}
            description={
              tab === 'notes'
                ? 'Notas internas são visíveis apenas para agentes.'
                : 'Envie a primeira mensagem para iniciar a conversa.'
            }
          />
        )}

        <div className="space-y-3">
          {filteredMessages.map((msg) => (
            <MessageBubble key={msg.id} message={msg} />
          ))}
          <div ref={bottomRef} />
        </div>
      </ScrollArea>

      {tab === 'notes' && isAgent ? (
        <NoteInput onSend={sendInternalNote} disabled={!active || !connected} />
      ) : (
        <MessageInput onSend={sendMessage} disabled={!active || !connected} />
      )}
    </div>
  )
}