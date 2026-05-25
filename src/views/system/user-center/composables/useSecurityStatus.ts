import { useSwrCache } from '@/hooks/core/useSwrCache'
import { fetchSecurityStatus } from '@/api/security'

const KEY = 'user-center:security-status'

export function useSecurityStatus() {
  const { data, loading, load, refresh, mutate } = useSwrCache<Api.User.Security.SecurityStatus>(
    KEY,
    fetchSecurityStatus
  )
  return { status: data, loading, load, refresh, mutate }
}
