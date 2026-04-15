import { useQuery } from '@tanstack/react-query'
import { searchTickets } from '@/api/search'

export function useSearch(query: string) {
  return useQuery({
    queryKey: ['search', query],
    queryFn: () => searchTickets(query),
    enabled: query.length >= 2,
  })
}
