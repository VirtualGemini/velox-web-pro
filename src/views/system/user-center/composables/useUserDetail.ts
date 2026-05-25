import { computed } from 'vue'
import { fetchGetUserDetail } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'
import { useSwrCache } from '@/hooks/core/useSwrCache'

const KEY = 'user-center:user-detail'

export function useUserDetail() {
  const userStore = useUserStore()

  const { data, loading, load, refresh, mutate } = useSwrCache<Partial<Api.Auth.UserDetail>>(
    KEY,
    fetchGetUserDetail
  )

  const detail = computed<Partial<Api.Auth.UserDetail>>(() => data.value || {})

  const patch = (next: Partial<Api.Auth.UserDetail>) => {
    mutate((prev) => ({ ...(prev || {}), ...next }))
  }

  const syncStoreBasic = (next: Partial<Api.Auth.UserInfo>) => {
    const d = data.value || {}
    userStore.setUserInfo({
      buttons: d.buttons || [],
      roles: d.roles || [],
      userId: d.userId || '',
      userName: d.userName || '',
      email: next.email ?? d.email ?? '',
      phone: next.phone ?? d.phone,
      avatar: next.avatar ?? d.avatar
    })
  }

  return { detail, userInfo: detail, loading, load, refresh, patch, syncStoreBasic }
}
