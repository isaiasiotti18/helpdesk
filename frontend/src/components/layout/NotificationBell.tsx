import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useNotifications, useUnreadCount, useMarkAsRead, useMarkAllAsRead } from '@/hooks/useNotifications'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { relativeDate } from '@/lib/date'
import type { NotificationItem } from '@/api/notifications'

function BellIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/>
      <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>
    </svg>
  )
}

function NotificationRow({ item, onRead }: { item: NotificationItem; onRead: (id: string) => void }) {
  const navigate = useNavigate()

  function handleClick() {
    if (!item.isRead) onRead(item.id)
    if (item.ticketId) navigate(`/tickets/${item.ticketId}`)
  }

  return (
    <button
      onClick={handleClick}
      className={`w-full text-left px-4 py-3 hover:bg-muted transition-colors border-b last:border-b-0 ${
        !item.isRead ? 'bg-blue-50/50' : ''
      }`}
    >
      <div className="flex items-start gap-2">
        {!item.isRead && <div className="w-2 h-2 rounded-full bg-blue-500 mt-1.5 shrink-0" />}
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium">{item.title}</p>
          {item.content && <p className="text-xs text-muted-foreground mt-0.5">{item.content}</p>}
          <p className="text-xs text-muted-foreground mt-1">{relativeDate(item.createdAt)}</p>
        </div>
      </div>
    </button>
  )
}

export function NotificationBell() {
  const [open, setOpen] = useState(false)
  const { data: unread } = useUnreadCount()
  const { data: notifications } = useNotifications()
  const markRead = useMarkAsRead()
  const markAll = useMarkAllAsRead()

  const count = unread ?? 0

  return (
    <div className="relative">
      <Button variant="ghost" size="icon" onClick={() => setOpen(!open)} className="relative">
        <BellIcon />
        {count > 0 && (
          <span className="absolute -top-1 -right-1 bg-destructive text-destructive-foreground text-xs rounded-full w-5 h-5 flex items-center justify-center">
            {count > 9 ? '9+' : count}
          </span>
        )}
      </Button>

      {open && (
        <>
          <div className="fixed inset-0 z-40" onClick={() => setOpen(false)} />
          <div className="absolute right-0 top-full mt-2 w-80 bg-card border rounded-lg shadow-lg z-50">
            <div className="flex items-center justify-between px-4 py-3 border-b">
              <h3 className="text-sm font-semibold">Notificações</h3>
              {count > 0 && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => markAll.mutate()}
                  className="text-xs"
                >
                  Marcar todas como lidas
                </Button>
              )}
            </div>
            <ScrollArea className="max-h-80">
              {!notifications?.content.length && (
                <p className="text-sm text-muted-foreground text-center py-8">Nenhuma notificação</p>
              )}
              {notifications?.content.map((item) => (
                <NotificationRow key={item.id} item={item} onRead={(id) => markRead.mutate(id)} />
              ))}
            </ScrollArea>
          </div>
        </>
      )}
    </div>
  )
}
