import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '@/stores/authStore'
import { getDashboardMetrics } from '@/api/metrics'

export function useDashboardMetrics() {
  const role = useAuthStore((s) => s.user?.role)

  return useQuery({
    queryKey: ['metrics', 'dashboard'],
    queryFn: getDashboardMetrics,
    enabled: role === 'ADMIN',
    refetchInterval: 60000, // refresh a cada 1 min
  })
}
