import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { createTicketSchema, type CreateTicketFormData } from '@/lib/validations/ticket'
import { useCreateTicket } from '@/hooks/useTickets'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

const priorities = [
  { value: 'LOW', label: 'Baixa' },
  { value: 'MEDIUM', label: 'Média' },
  { value: 'HIGH', label: 'Alta' },
  { value: 'URGENT', label: 'Urgente' },
]

export function CreateTicketPage() {
  const navigate = useNavigate()
  const createTicket = useCreateTicket()

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<CreateTicketFormData>({
    resolver: zodResolver(createTicketSchema),
    defaultValues: { priority: 'MEDIUM' },
  })

  const selectedPriority = watch('priority')

  async function onSubmit(data: CreateTicketFormData) {
    await createTicket.mutateAsync(data)
    navigate('/tickets')
  }

  return (
    <div className="p-6 max-w-2xl">
      <Card>
        <CardHeader>
          <CardTitle>Novo Ticket</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="title">Título</Label>
              <Input id="title" {...register('title')} />
              {errors.title && (
                <p className="text-sm text-destructive">{errors.title.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Descrição</Label>
              <Textarea id="description" rows={4} {...register('description')} />
            </div>

            <div className="space-y-2">
              <Label>Prioridade</Label>
              <div className="flex gap-2">
                {priorities.map((p) => (
                  <Button
                    key={p.value}
                    type="button"
                    variant={selectedPriority === p.value ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => setValue('priority', p.value as CreateTicketFormData['priority'])}
                  >
                    {p.label}
                  </Button>
                ))}
              </div>
              {errors.priority && (
                <p className="text-sm text-destructive">{errors.priority.message}</p>
              )}
            </div>

            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Criando...' : 'Criar Ticket'}
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate('/tickets')}>
                Cancelar
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}