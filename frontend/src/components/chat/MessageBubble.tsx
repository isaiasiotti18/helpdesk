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

  if (isSystem) {
    return (
      <div className="text-center">
        <span className="text-xs text-muted-foreground bg-muted px-3 py-1 rounded-full">
          {message.content}
        </span>
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