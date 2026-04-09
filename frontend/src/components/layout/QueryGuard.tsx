import type { UseQueryResult } from '@tanstack/react-query'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { Button } from '@/components/ui/button'
import type { ReactNode } from 'react'

interface QueryGuardProps<T> {
  query: UseQueryResult<T>
  children: (data: T) => ReactNode
  emptyCheck?: (data: T) => boolean
  emptyMessage?: string
  emptyAction?: { label: string; onClick: () => void }
  skeleton?: ReactNode  // ← novo
}

export function QueryGuard<T>({
  query,
  children,
  emptyCheck,
  emptyMessage = 'Nenhum resultado encontrado.',
  emptyAction,
  skeleton,
}: QueryGuardProps<T>) {
  if (query.isLoading) return skeleton ? <>{skeleton}</> : <LoadingSpinner />

  if (query.isError) {
    return (
      <div className="flex flex-col items-center gap-3 p-8 text-center">
        <p className="text-sm text-destructive">Erro ao carregar dados.</p>
        <Button variant="outline" size="sm" onClick={() => query.refetch()}>
          Tentar novamente
        </Button>
      </div>
    )
  }

  if (!query.data || (emptyCheck && emptyCheck(query.data))) {
    return (
      <div className="flex flex-col items-center gap-3 p-8 text-center">
        <p className="text-sm text-muted-foreground">{emptyMessage}</p>
        {emptyAction && (
          <Button variant="outline" size="sm" onClick={emptyAction.onClick}>
            {emptyAction.label}
          </Button>
        )}
      </div>
    )
  }

  return <>{children(query.data)}</>
}