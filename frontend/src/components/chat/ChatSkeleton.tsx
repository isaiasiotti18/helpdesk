import { Skeleton } from '@/components/ui/skeleton'

export function ChatSkeleton() {
  return (
    <div className="space-y-4 p-4">
      {/* Mensagem esquerda */}
      <div className="flex justify-start">
        <div className="space-y-2">
          <Skeleton className="h-3 w-16" />
          <Skeleton className="h-16 w-56 rounded-lg" />
        </div>
      </div>
      {/* Mensagem direita */}
      <div className="flex justify-end">
        <Skeleton className="h-12 w-48 rounded-lg" />
      </div>
      {/* Mensagem esquerda */}
      <div className="flex justify-start">
        <div className="space-y-2">
          <Skeleton className="h-3 w-16" />
          <Skeleton className="h-20 w-64 rounded-lg" />
        </div>
      </div>
      {/* Mensagem direita */}
      <div className="flex justify-end">
        <Skeleton className="h-10 w-40 rounded-lg" />
      </div>
    </div>
  )
}