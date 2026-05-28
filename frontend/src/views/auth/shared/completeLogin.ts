import { ElNotification } from 'element-plus'
import type { Router } from 'vue-router'
import type { useAccountStore } from '@/store/modules/user'

interface CompleteLoginOptions {
  accountStore: ReturnType<typeof useAccountStore>
  token: string
  refreshToken?: string
  redirect?: string
  router: Router
  successTitle: string
  successMessage: string
}

/**
 * 统一登录完成流程：
 * token 入库 -> 拉取正式用户信息 -> 标记登录态 -> 跳转 -> 展示欢迎提示
 */
export async function completeLogin({
  accountStore,
  token,
  refreshToken,
  redirect,
  router,
  successTitle,
  successMessage
}: CompleteLoginOptions): Promise<void> {
  accountStore.setToken(token, refreshToken)

  try {
    const accountInfo = await accountStore.hydrateAccountInfo({ force: true })
    accountStore.setPostLoginNavigating(true)
    accountStore.setLoginStatus(true)

    await router.push(redirect || '/')
    accountStore.setPostLoginNavigating(false)

    window.setTimeout(() => {
      ElNotification({
        title: successTitle,
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: accountInfo.userName ? `${successMessage}, ${accountInfo.userName}!` : successMessage
      })
    }, 80)
  } catch (error) {
    accountStore.clearAuthState()
    throw error
  }
}
