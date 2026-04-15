import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { createCategorySchema, type CreateCategoryFormData } from '@/lib/validations/category'
import { useAllCategories, useCreateCategory, useDeleteCategory } from '@/hooks/useCategories'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { FormField } from '@/components/ui/form-field'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { EmptyState } from '@/components/layout/EmptyState'
import { relativeDate } from '@/lib/date'

export function CategoriesPage() {
  const { data: categories, isLoading } = useAllCategories()
  const createCategory = useCreateCategory()
  const deleteCategory = useDeleteCategory()
  const [showForm, setShowForm] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CreateCategoryFormData>({
    resolver: zodResolver(createCategorySchema),
  })

  async function onSubmit(data: CreateCategoryFormData) {
    await createCategory.mutateAsync({
      name: data.name,
      description: data.description,
      queueId: data.queueId || undefined,
    })
    reset()
    setShowForm(false)
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Categorias</h1>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancelar' : 'Nova Categoria'}
        </Button>
      </div>

      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle>Nova Categoria</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                label="Nome"
                registration={register('name')}
                error={errors.name}
              />
              <div className="space-y-2">
                <label className="text-sm font-medium">Descrição</label>
                <textarea
                  {...register('description')}
                  rows={3}
                  className="flex w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                />
              </div>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Criando...' : 'Criar'}
              </Button>
            </form>
          </CardContent>
        </Card>
      )}

      {isLoading && <LoadingSpinner />}

      {!isLoading && categories?.length === 0 && (
        <EmptyState
          title="Nenhuma categoria"
          description="Crie categorias para organizar seus tickets por departamento."
          action={{ label: 'Criar categoria', onClick: () => setShowForm(true) }}
        />
      )}

      {categories && categories.length > 0 && (
        <div className="space-y-3">
          {categories.map((cat) => (
            <Card key={cat.id}>
              <CardContent className="p-4 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-medium">{cat.name}</h3>
                    <Badge variant={cat.isActive ? 'default' : 'outline'}>
                      {cat.isActive ? 'Ativa' : 'Inativa'}
                    </Badge>
                    {cat.queueName && (
                      <Badge variant="secondary">{cat.queueName}</Badge>
                    )}
                  </div>
                  {cat.description && (
                    <p className="text-sm text-muted-foreground mt-1">{cat.description}</p>
                  )}
                  <p className="text-xs text-muted-foreground mt-1">
                    Criada {relativeDate(cat.createdAt)}
                  </p>
                </div>
                {cat.isActive && (
                  <ConfirmDialog
                    trigger={
                      <Button variant="outline" size="sm">
                        Desativar
                      </Button>
                    }
                    title="Desativar categoria?"
                    description="Tickets existentes não serão afetados, mas novos tickets não poderão usar esta categoria."
                    confirmLabel="Desativar"
                    onConfirm={() => deleteCategory.mutate(cat.id)}
                  />
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
