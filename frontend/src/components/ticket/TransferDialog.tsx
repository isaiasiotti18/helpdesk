import { useState } from 'react'
import { useTransferToAgent, useTransferToQueue } from '@/hooks/useTransfer'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'

interface TransferDialogProps {
  ticketId: string
  queues: Array<{ id: string; name: string }>
  agents: Array<{ id: string; name: string }>
}

export function TransferDialog({ ticketId, queues, agents }: TransferDialogProps) {
  const [mode, setMode] = useState<'agent' | 'queue'>('agent')
  const [selectedId, setSelectedId] = useState('')
  const [reason, setReason] = useState('')

  const transferAgent = useTransferToAgent()
  const transferQueue = useTransferToQueue()

  function handleTransfer() {
    if (!selectedId) return

    if (mode === 'agent') {
      transferAgent.mutate({
        ticketId,
        payload: { agentId: selectedId, reason: reason || undefined },
      })
    } else {
      transferQueue.mutate({
        ticketId,
        payload: { queueId: selectedId, reason: reason || undefined },
      })
    }

    setSelectedId('')
    setReason('')
  }

  const isPending = transferAgent.isPending || transferQueue.isPending

  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button variant="outline" size="sm">
          Transferir
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Transferir ticket</AlertDialogTitle>
          <AlertDialogDescription>
            Escolha transferir para outro agente ou para outra fila de atendimento.
          </AlertDialogDescription>
        </AlertDialogHeader>

        <div className="space-y-4 py-2">
          <div className="flex gap-2">
            <Button
              variant={mode === 'agent' ? 'default' : 'outline'}
              size="sm"
              onClick={() => { setMode('agent'); setSelectedId('') }}
            >
              Para agente
            </Button>
            <Button
              variant={mode === 'queue' ? 'default' : 'outline'}
              size="sm"
              onClick={() => { setMode('queue'); setSelectedId('') }}
            >
              Para fila
            </Button>
          </div>

          <div className="space-y-2">
            <Label>{mode === 'agent' ? 'Agente' : 'Fila'}</Label>
            <select
              value={selectedId}
              onChange={(e) => setSelectedId(e.target.value)}
              className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            >
              <option value="">Selecione...</option>
              {mode === 'agent'
                ? agents.map((a) => (
                    <option key={a.id} value={a.id}>{a.name}</option>
                  ))
                : queues.map((q) => (
                    <option key={q.id} value={q.id}>{q.name}</option>
                  ))
              }
            </select>
          </div>

          <div className="space-y-2">
            <Label>Motivo (opcional)</Label>
            <Textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={2}
              placeholder="Ex: Cliente precisa de suporte técnico especializado"
            />
          </div>
        </div>

        <AlertDialogFooter>
          <AlertDialogCancel>Cancelar</AlertDialogCancel>
          <AlertDialogAction
            onClick={handleTransfer}
            disabled={!selectedId || isPending}
          >
            {isPending ? 'Transferindo...' : 'Transferir'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
