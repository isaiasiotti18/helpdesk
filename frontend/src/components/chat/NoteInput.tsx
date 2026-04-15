import { useState, type KeyboardEvent } from 'react'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'

interface NoteInputProps {
  onSend: (content: string) => void
  disabled?: boolean
}

export function NoteInput({ onSend, disabled }: NoteInputProps) {
  const [content, setContent] = useState('')

  function handleSend() {
    const trimmed = content.trim()
    if (!trimmed) return
    onSend(trimmed)
    setContent('')
  }

  function handleKeyDown(e: KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="flex gap-2 p-4 border-t bg-amber-50/50">
      <Textarea
        value={content}
        onChange={(e) => setContent(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="Nota interna (só agentes veem)..."
        disabled={disabled}
        rows={1}
        className="resize-none min-h-[40px] bg-white"
      />
      <Button
        onClick={handleSend}
        disabled={disabled || !content.trim()}
        variant="outline"
        className="border-amber-300 text-amber-700 hover:bg-amber-50"
      >
        Nota
      </Button>
    </div>
  )
}