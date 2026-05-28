import { useSwrCache } from '@/hooks/core/useSwrCache'
import { fetchSecurityStatus } from '@/api/security'
import { useAccountStore } from '@/store/modules/user'

export function useSecurityStatus() {
  const accountStore = useAccountStore()
  const activeUserId = accountStore.activeUserId || accountStore.info.accountId || 'anonymous'
  const key = `account-center:security-status:${activeUserId}`
  const { data, loading, load, refresh, mutate } = useSwrCache<Api.User.Security.SecurityStatus>(
    key,
    fetchSecurityStatus,
    {
      visible: () => accountStore.canReadUserCache
    }
  )
  return { status: data, loading, load, refresh, mutate }
}
