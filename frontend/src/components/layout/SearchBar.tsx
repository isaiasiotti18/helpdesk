import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSearch } from '@/hooks/useSearch'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { ScrollArea } from '@/components/ui/scroll-area'
import { STATUS_LABELS, PRIORITY_LABELS } from '@/lib/constants'
import { relativeDate } from '@/lib/date'
import type { TicketStatus, Priority } from '@/types'

export function SearchBar() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const [debounced, setDebounced] = useState('')

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(query), 300)
    return () => clearTimeout(timer)
  }, [query])

  const { data: results, isLoading } = useSearch(debounced)

  function handleSelect(ticketId: string) {
    setQuery('')
    setOpen(false)
    navigate(`/tickets/${ticketId}`)
  }

  return (
    <div className="relative w-full max-w-md">
      <Input
        value={query}
        onChange={(e) => {
          setQuery(e.target.value)
          setOpen(e.target.value.length >= 2)
        }}
        onFocus={() => query.length >= 2 && setOpen(true)}
        placeholder="Buscar tickets..."
        className="w-full"
      />

      {open && (
        <>
          <div className="fixed inset-0 z-40" onClick={() => setOpen(false)} />
          <div className="absolute top-full left-0 right-0 mt-1 bg-card border rounded-lg shadow-lg z-50 overflow-hidden">
            {isLoading && (
              <p className="text-sm text-muted-foreground text-center py-4">Buscando...</p>
            )}

            {!isLoading && results?.length === 0 && (
              <p className="text-sm text-muted-foreground text-center py-4">Nenhum resultado</p>
            )}

            {results && results.length > 0 && (
              <ScrollArea className="max-h-72">
                {results.map((r) => (
                  <button
                    key={r.id}
                    onClick={() => handleSelect(r.id)}
                    className="w-full text-left px-4 py-3 hover:bg-muted transition-colors border-b last:border-b-0"
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{r.title}</p>
                        <p className="text-xs text-muted-foreground mt-0.5">
                          {r.createdByName} · {relativeDate(r.createdAt)}
                          {r.categoryName && ` · ${r.categoryName}`}
                        </p>
                      </div>
                      <div className="flex gap-1 shrink-0">
                        <Badge variant="outline" className="text-xs">
                          {PRIORITY_LABELS[r.priority as Priority]}
                        </Badge>
                        <Badge variant="secondary" className="text-xs">
                          {STATUS_LABELS[r.status as TicketStatus]}
                        </Badge>
                      </div>
                    </div>
                  </button>
                ))}
              </ScrollArea>
            )}
          </div>
        </>
      )}
    </div>
  )
}
