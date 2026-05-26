import { useSwrCache } from '@/hooks/core/useSwrCache'
import { fetchSecurityStatus } from '@/api/security'
import { useUserStore } from '@/store/modules/user'

export function useSecurityStatus() {
  const userStore = useUserStore()
  const activeUserId = userStore.activeUserId || userStore.info.userId || 'anonymous'
  const key = `user-center:security-status:${activeUserId}`
  const { data, loading, load, refresh, mutate } = useSwrCache<Api.User.Security.SecurityStatus>(
    key,
    fetchSecurityStatus,
    {
      visible: () => userStore.canReadUserCache
    }
  )
  return { status: data, loading, load, refresh, mutate }
}
