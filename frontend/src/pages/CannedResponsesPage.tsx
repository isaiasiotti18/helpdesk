import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { createCannedSchema, type CreateCannedFormData } from '@/lib/validations/canned'
import { useCannedResponses, useCreateCanned, useDeleteCanned } from '@/hooks/useCannedResponses'
import { FormField } from '@/components/ui/form-field'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { EmptyState } from '@/components/layout/EmptyState'

export function CannedResponsesPage() {
  const { data: responses, isLoading } = useCannedResponses()
  const createCanned = useCreateCanned()
  const deleteCanned = useDeleteCanned()
  const [showForm, setShowForm] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CreateCannedFormData>({
    resolver: zodResolver(createCannedSchema),
    defaultValues: { isShared: false },
  })

  async function onSubmit(data: CreateCannedFormData) {
    await createCanned.mutateAsync({
      title: data.title,
      content: data.content,
      shortcut: data.shortcut || undefined,
      category: data.category || undefined,
      isShared: data.isShared,
    })
    reset()
    setShowForm(false)
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Respostas Prontas</h1>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancelar' : 'Nova Resposta'}
        </Button>
      </div>

      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle>Nova Resposta Pronta</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-lg">
              <FormField label="Título" registration={register('title')} error={errors.title} />
              <div className="space-y-2">
                <label className="text-sm font-medium">Conteúdo</label>
                <textarea
                  {...register('content')}
                  rows={4}
                  className="flex w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                  placeholder="Texto que será inserido no chat..."
                />
                {errors.content && <p className="text-sm text-destructive">{errors.content.message}</p>}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  label="Atalho (ex: saudacao)"
                  registration={register('shortcut')}
                  error={errors.shortcut}
                  placeholder="/saudacao"
                />
                <FormField
                  label="Categoria"
                  registration={register('category')}
                  error={errors.category}
                  placeholder="Geral"
                />
              </div>
              <div className="flex items-center gap-2">
                <input type="checkbox" id="isShared" {...register('isShared')} className="rounded" />
                <label htmlFor="isShared" className="text-sm">Compartilhar com a equipe</label>
              </div>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Criando...' : 'Criar'}
              </Button>
            </form>
          </CardContent>
        </Card>
      )}

      {isLoading && <LoadingSpinner />}

      {!isLoading && responses?.length === 0 && (
        <EmptyState
          title="Nenhuma resposta pronta"
          description="Crie respostas prontas para agilizar o atendimento. Use / no chat para acessá-las."
          action={{ label: 'Criar resposta', onClick: () => setShowForm(true) }}
        />
      )}

      {responses && responses.length > 0 && (
        <div className="space-y-3">
          {responses.map((r) => (
            <Card key={r.id}>
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-medium">{r.title}</h3>
                      {r.shortcut && (
                        <Badge variant="outline">/{r.shortcut}</Badge>
                      )}
                      {r.isShared && (
                        <Badge variant="secondary">Compartilhada</Badge>
                      )}
                      {r.category && (
                        <Badge variant="outline">{r.category}</Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground whitespace-pre-wrap">{r.content}</p>
                    <p className="text-xs text-muted-foreground mt-2">
                      Criada por {r.createdByName}
                    </p>
                  </div>
                  <ConfirmDialog
                    trigger={<Button variant="outline" size="sm">Remover</Button>}
                    title="Remover resposta pronta?"
                    description={`"${r.title}" será removida permanentemente.`}
                    confirmLabel="Remover"
                    onConfirm={() => deleteCanned.mutate(r.id)}
                  />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
