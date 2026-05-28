import { computed } from 'vue'
import { fetchGetAccountDetail } from '@/api/auth'
import { useAccountStore } from '@/store/modules/user'
import { useSwrCache } from '@/hooks/core/useSwrCache'

export function useAccountDetail() {
  const accountStore = useAccountStore()
  const activeUserId = accountStore.activeUserId || accountStore.info.accountId || 'anonymous'
  const key = `account-center:account-detail:${activeUserId}`

  const { data, loading, load, refresh, mutate } = useSwrCache<Partial<Api.Auth.AccountDetail>>(
    key,
    fetchGetAccountDetail,
    {
      visible: () => accountStore.canReadUserCache
    }
  )

  const detail = computed<Partial<Api.Auth.AccountDetail>>(() => data.value || {})

  const patch = (next: Partial<Api.Auth.AccountDetail>) => {
    mutate((prev) => ({ ...(prev || {}), ...next }))
  }

  const syncStoreBasic = (next: Partial<Api.Auth.AccountInfo>) => {
    const d = data.value || {}
    accountStore.setAccountInfo({
      buttons: d.buttons || [],
      roles: d.roles || [],
      accountId: d.accountId || '',
      userName: d.userName || '',
      email: next.email ?? d.email ?? '',
      phone: next.phone ?? d.phone,
      avatar: next.avatar ?? d.avatar
    })
  }

  return { detail, accountInfo: detail, userInfo: detail, loading, load, refresh, patch, syncStoreBasic }
}

export const useUserDetail = useAccountDetail
