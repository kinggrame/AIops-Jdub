import { useQueries } from '@tanstack/react-query'
import { fetchAgents, fetchAlerts, fetchCommandResults } from '../api/aiops'

export function useDashboardData() {
  const results = useQueries({
    queries: [
      { queryKey: ['agents'], queryFn: fetchAgents },
      { queryKey: ['alerts'], queryFn: fetchAlerts },
      { queryKey: ['command-results'], queryFn: fetchCommandResults },
    ],
  })

  return {
    agents: results[0],
    alerts: results[1],
    commandResults: results[2],
    isLoading: results.some((result) => result.isLoading),
    isError: results.some((result) => result.isError),
    refetchAll: () => results.forEach((result) => result.refetch()),
  }
}
