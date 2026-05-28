import { computed } from 'vue'
import { fetchGetAccountTabInfo } from '@/api/auth'
import { useAccountStore } from '@/store/modules/user'
import { useSwrCache } from '@/hooks/core/useSwrCache'

export function useAccountTab() {
  const accountStore = useAccountStore()
  const activeUserId = accountStore.activeUserId || accountStore.info.accountId || 'anonymous'
  const key = `account-center:account-tab:${activeUserId}`

  const { data, loading, load, refresh, mutate } = useSwrCache<Partial<Api.Auth.AccountTabInfo>>(
    key,
    fetchGetAccountTabInfo,
    {
      visible: () => accountStore.canReadUserCache
    }
  )

  const detail = computed<Partial<Api.Auth.AccountTabInfo>>(() => data.value || {})

  const patch = (next: Partial<Api.Auth.AccountTabInfo>) => {
    mutate((prev) => ({ ...(prev || {}), ...next }))
  }

  return { detail, loading, load, refresh, patch }
}
