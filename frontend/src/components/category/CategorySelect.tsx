import { useActiveCategories } from '@/hooks/useCategories'

interface CategorySelectProps {
  value: string
  onChange: (value: string) => void
  error?: string
}

export function CategorySelect({ value, onChange, error }: CategorySelectProps) {
  const { data: categories, isLoading } = useActiveCategories()

  return (
    <div className="space-y-2">
      <label className="text-sm font-medium">Categoria</label>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={isLoading}
        className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
      >
        <option value="">Selecione uma categoria</option>
        {categories?.map((cat) => (
          <option key={cat.id} value={cat.id}>
            {cat.name}
          </option>
        ))}
      </select>
      {error && <p className="text-sm text-destructive">{error}</p>}
    </div>
  )
}
