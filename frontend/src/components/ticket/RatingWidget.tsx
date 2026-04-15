import { useState } from 'react'
import { useTicketRating, useRateTicket } from '@/hooks/useRating'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

interface RatingWidgetProps {
  ticketId: string
  canRate: boolean // só o criador do ticket pode avaliar
}

function StarButton({ filled, onClick }: { filled: boolean; onClick: () => void }) {
  return (
    <button onClick={onClick} className="text-2xl transition-colors hover:scale-110">
      {filled ? '★' : '☆'}
    </button>
  )
}

function StarDisplay({ score }: { score: number }) {
  return (
    <div className="flex gap-0.5 text-amber-500 text-xl">
      {[1, 2, 3, 4, 5].map((i) => (
        <span key={i}>{i <= score ? '★' : '☆'}</span>
      ))}
    </div>
  )
}

export function RatingWidget({ ticketId, canRate }: RatingWidgetProps) {
  const { data: existing, isLoading, isError } = useTicketRating(ticketId)
  const rateMutation = useRateTicket()

  const [score, setScore] = useState(0)
  const [comment, setComment] = useState('')
  const [showForm, setShowForm] = useState(false)

  if (isLoading) return null

  // Já avaliou
  if (existing && !isError) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Avaliação</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <StarDisplay score={existing.score} />
          {existing.comment && (
            <p className="text-sm text-muted-foreground">{existing.comment}</p>
          )}
          <p className="text-xs text-muted-foreground">
            Avaliado por {existing.userName}
          </p>
        </CardContent>
      </Card>
    )
  }

  // Não pode avaliar (não é o criador ou ticket não fechado)
  if (!canRate) return null

  // Formulário de avaliação
  if (!showForm) {
    return (
      <Card className="border-dashed">
        <CardContent className="p-4 text-center space-y-3">
          <p className="text-sm text-muted-foreground">Como foi o atendimento?</p>
          <Button variant="outline" onClick={() => setShowForm(true)}>
            Avaliar
          </Button>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Avalie o atendimento</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex gap-1 text-amber-500">
          {[1, 2, 3, 4, 5].map((i) => (
            <StarButton key={i} filled={i <= score} onClick={() => setScore(i)} />
          ))}
        </div>
        <Textarea
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Comentário (opcional)"
          rows={2}
        />
        <div className="flex gap-2">
          <Button
            disabled={score === 0 || rateMutation.isPending}
            onClick={() => rateMutation.mutate({ ticketId, score, comment: comment || undefined })}
          >
            {rateMutation.isPending ? 'Enviando...' : 'Enviar'}
          </Button>
          <Button variant="outline" onClick={() => setShowForm(false)}>
            Cancelar
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
