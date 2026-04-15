import { chatTime } from '@/lib/date'
import { useAuthStore } from '@/stores/authStore'
import type { Message } from '@/types'

interface MessageBubbleProps {
  message: Message
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const userId = useAuthStore((s) => s.user?.id)
  const isMine = message.senderId === userId
  const isSystem = message.messageType === 'SYSTEM'
  const isNote = message.isInternal || message.messageType === 'NOTE'

  if (isSystem) {
    return (
      <div className="text-center">
        <span className="text-xs text-muted-foreground bg-muted px-3 py-1 rounded-full">
          {message.content}
        </span>
      </div>
    )
  }

  if (isNote) {
    return (
      <div className="flex justify-center">
        <div className="max-w-[80%] rounded-lg px-4 py-2 bg-amber-50 border border-amber-200">
          <div className="flex items-center gap-2 mb-1">
            <span className="text-xs font-medium text-amber-700">Nota interna</span>
            <span className="text-xs text-amber-500">— {message.senderName}</span>
          </div>
          <p className="text-sm text-amber-900">{message.content}</p>
          <p className="text-xs text-amber-400 mt-1">{chatTime(message.sentAt)}</p>
        </div>
      </div>
    )
  }

  return (
    <div className={`flex ${isMine ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`max-w-[70%] rounded-lg px-4 py-2 ${isMine
          ? 'bg-primary text-primary-foreground'
          : 'bg-muted'
          }`}
      >
        {!isMine && (
          <p className="text-xs font-medium mb-1 opacity-70">{message.senderName}</p>
        )}
        <p className="text-sm break-words">{message.content}</p>
        <p className={`text-xs mt-1 ${isMine ? 'text-primary-foreground/70' : 'text-muted-foreground'}`}>
          {chatTime(message.sentAt)}
        </p>
      </div>
    </div>
  )
}