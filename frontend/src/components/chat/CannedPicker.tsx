import { useState, useEffect, useRef } from 'react'
import { useSearchCanned, useCannedResponses } from '@/hooks/useCannedResponses'
import { ScrollArea } from '@/components/ui/scroll-area'
import type { CannedResponse } from '@/api/canned'

interface CannedPickerProps {
  query: string
  onSelect: (content: string) => void
  onClose: () => void
}

export function CannedPicker({ query, onSelect, onClose }: CannedPickerProps) {
  const [selectedIndex, setSelectedIndex] = useState(0)
  const listRef = useRef<HTMLDivElement>(null)

  const searchQuery = query.startsWith('/') ? query.slice(1) : query
  const { data: searchResults } = useSearchCanned(searchQuery)
  const { data: allResults } = useCannedResponses()

  const results = searchQuery.length >= 1 ? searchResults : allResults
  const items = results ?? []

  useEffect(() => {
    setSelectedIndex(0)
  }, [query])

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'ArrowDown') {
        e.preventDefault()
        setSelectedIndex((i) => Math.min(i + 1, items.length - 1))
      } else if (e.key === 'ArrowUp') {
        e.preventDefault()
        setSelectedIndex((i) => Math.max(i - 1, 0))
      } else if (e.key === 'Enter' && items.length > 0) {
        e.preventDefault()
        onSelect(items[selectedIndex].content)
      } else if (e.key === 'Escape') {
        onClose()
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [items, selectedIndex, onSelect, onClose])

  if (items.length === 0) {
    return (
      <div className="absolute bottom-full left-0 right-0 mb-1 bg-card border rounded-lg shadow-lg p-3">
        <p className="text-sm text-muted-foreground">Nenhuma resposta pronta encontrada.</p>
      </div>
    )
  }

  return (
    <div className="absolute bottom-full left-0 right-0 mb-1 bg-card border rounded-lg shadow-lg overflow-hidden">
      <div className="px-3 py-2 border-b">
        <p className="text-xs text-muted-foreground">Respostas prontas — ↑↓ navegar · Enter selecionar · Esc fechar</p>
      </div>
      <ScrollArea className="max-h-48">
        <div ref={listRef}>
          {items.map((item: CannedResponse, index: number) => (
            <button
              key={item.id}
              onClick={() => onSelect(item.content)}
              className={`w-full text-left px-3 py-2 hover:bg-muted transition-colors ${
                index === selectedIndex ? 'bg-muted' : ''
              }`}
            >
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium">{item.title}</span>
                {item.shortcut && (
                  <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
                    /{item.shortcut}
                  </span>
                )}
                {item.isShared && (
                  <span className="text-xs text-blue-500">compartilhada</span>
                )}
              </div>
              <p className="text-xs text-muted-foreground mt-0.5 truncate">{item.content}</p>
            </button>
          ))}
        </div>
      </ScrollArea>
    </div>
  )
}
