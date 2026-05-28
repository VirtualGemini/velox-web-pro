<!-- 授权页：已登录账号卡片守卫，挂载时若已登录则替换路由内容 -->
<template>
  <template v-if="showLoggedInCard">
    <h3 class="title">{{ $t('login.loggedIn.title') }}</h3>
    <p class="sub-title">{{ $t('login.loggedIn.subTitle') }}</p>
    <p v-if="isPendingDeletionCard" class="pending-deletion-tip">
      {{ $t('login.loggedIn.pendingDeletionWarning') }}
    </p>
    <div style="margin-top: 25px">
      <VeloxLoggedInCard
        :user-info="accountInfo"
        :selected="accountSelected"
        :logout-visible="!isPendingDeletionCard"
        @select="accountSelected = !accountSelected"
        @logout="handleLogout"
      />
    </div>
    <div style="margin-top: 30px">
      <ElButton class="w-full custom-height" type="primary" @click="handleContinue" v-ripple>
        {{ $t('login.loggedIn.continueBtn') }}
      </ElButton>
    </div>
    <div v-if="isPendingDeletionCard" style="margin-top: 12px">
      <ElButton class="w-full custom-height" @click="handleCancel" v-ripple>
        {{ $t('common.cancel') }}
      </ElButton>
    </div>
  </template>
  <slot v-else />
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useAccountStore } from '@/store/modules/user'
  import { fetchRecoverAccount } from '@/api/auth'

  defineOptions({ name: 'AuthLoggedInGuard' })

  const { t } = useI18n()
  const router = useRouter()
  const route = useRoute()
  const accountStore = useAccountStore()
  const { getAccountInfo: accountInfo } = storeToRefs(accountStore)
  const pendingDeletionLogin = computed(() => accountStore.pendingDeletionLogin)

  const showLoggedInCard = computed(
    () =>
      ((accountStore.isLogin && !!accountStore.accessToken) || !!pendingDeletionLogin.value?.pendingDeletion) &&
      !accountStore.isPostLoginNavigating
  )
  const accountSelected = ref(false)
  const isPendingDeletionCard = computed(() => !!pendingDeletionLogin.value?.pendingDeletion)

  const handleContinue = async () => {
    if (!accountSelected.value) {
      ElMessage.warning(t('login.loggedIn.selectTip'))
      return
    }
    if (isPendingDeletionCard.value) {
      try {
        const username = pendingDeletionLogin.value?.userName || ''
        const { value } = await ElMessageBox.prompt(
          t('login.loggedIn.pendingDeletionConfirmDesc', { username }),
          t('login.loggedIn.pendingDeletionConfirmTitle'),
          {
            confirmButtonText: t('common.confirm'),
            cancelButtonText: t('common.cancel'),
            inputValue: '',
            inputValidator: (inputValue) => {
              if (!inputValue || inputValue.trim() !== username) {
                return t('login.loggedIn.pendingDeletionConfirmError')
              }
              return true
            }
          }
        )
        await fetchRecoverAccount({ username: value.trim() })
        await accountStore.hydrateAccountInfo({ force: true })
        accountStore.setLoginStatus(true)
        accountStore.setPendingDeletionLogin(null)
        const redirect = route.query.redirect as string | undefined
        router.push(redirect || '/')
      } catch {
        await accountStore.logOut()
        return
      }
      return
    }
    const redirect = route.query.redirect as string | undefined
    router.push(redirect || '/')
  }

  const handleLogout = () => {
    if (isPendingDeletionCard.value) {
      accountStore.setPendingDeletionLogin(null)
      accountSelected.value = false
      return
    }
    ElMessageBox.confirm(t('common.logOutTips'), t('common.tips'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      customClass: 'login-out-dialog'
    })
      .then(() => {
        accountStore.logOut()
      })
      .catch(() => {})
  }

  const handleCancel = () => {
    accountStore.setPendingDeletionLogin(null)
    accountSelected.value = false
    router.replace('/login')
  }
</script>

<style scoped>
  @reference '@styles/core/tailwind.css';

  .title {
    @apply text-g-900 text-4xl font-semibold max-md:text-3xl max-sm:pt-10;
  }

  .sub-title {
    @apply mt-[10px] text-g-600 text-sm;
  }

  .pending-deletion-tip {
    @apply mt-2 text-[12px] leading-5 text-danger;
  }

  .custom-height {
    @apply !h-[40px];
  }
</style>
