import { computed } from 'vue'
import { fetchGetUserDetail } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'
import { useSwrCache } from '@/hooks/core/useSwrCache'

export function useUserDetail() {
  const userStore = useUserStore()
  const activeUserId = userStore.activeUserId || userStore.info.userId || 'anonymous'
  const key = `user-center:user-detail:${activeUserId}`

  const { data, loading, load, refresh, mutate } = useSwrCache<Partial<Api.Auth.UserDetail>>(
    key,
    fetchGetUserDetail,
    {
      visible: () => userStore.canReadUserCache
    }
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
