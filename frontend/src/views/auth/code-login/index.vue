<!-- 验证码登录页：选择登录方式 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <AuthLoggedInGuard>
            <h3 class="title">{{ $t('login.channel.title') }}</h3>
            <p class="sub-title">{{ $t('login.channel.subTitle') }}</p>

            <div class="mt-7.5 flex flex-col gap-3">
              <div v-if="emailEnabled" class="channel-card" @click="selectChannel('email')">
                <div class="channel-icon channel-icon--bare">
                  <img
                    :src="emailIcon"
                    :alt="$t('login.channel.email')"
                    style="width: 24px; height: 24px"
                  />
                </div>
                <div class="channel-text">
                  <div class="channel-title">{{ $t('login.channel.email') }}</div>
                </div>
                <VeloxSvgIcon icon="ri:arrow-right-s-line" class="channel-arrow" />
              </div>

              <div v-if="isChannelEnabled('github')" class="channel-card channel-card--disabled">
                <div class="channel-icon channel-icon--bare">
                  <img
                    :src="githubIcon"
                    :alt="$t('login.channel.github')"
                    style="width: 24px; height: 24px"
                  />
                </div>
                <div class="channel-text">
                  <div class="channel-title">{{ $t('login.channel.github') }}</div>
                </div>
                <span class="channel-tag">{{ $t('login.channel.comingSoon') }}</span>
              </div>

              <div v-if="isChannelEnabled('linuxdo')" class="channel-card channel-card--disabled">
                <div class="channel-icon channel-icon--bare">
                  <img
                    :src="linuxdoIcon"
                    :alt="$t('login.channel.linuxdo')"
                    style="width: 24px; height: 24px"
                  />
                </div>
                <div class="channel-text">
                  <div class="channel-title">{{ $t('login.channel.linuxdo') }}</div>
                </div>
                <span class="channel-tag">{{ $t('login.channel.comingSoon') }}</span>
              </div>
            </div>

            <div class="mt-7.5 text-sm text-g-600">
              <RouterLink class="text-theme" :to="{ name: 'Login' }">{{
                $t('login.passwordLogin')
              }}</RouterLink>
            </div>
          </AuthLoggedInGuard>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import githubDarkIcon from '@/assets/images/svg/github.svg'
  import githubLightIcon from '@/assets/images/svg/github-light.svg'
  import gmailCleanerDarkIcon from '@/assets/images/svg/gmail-cleaner.svg'
  import gmailCleanerLightIcon from '@/assets/images/svg/gmail-cleaner-light.svg'
  import linuxdoIcon from '@/assets/images/svg/linuxdo.svg'
  import { useSettingStore } from '@/store/modules/setting'
  import { grantAuthRouteAccess } from '../shared/routeAccess'
  import { fetchGetAccessConfig } from '@/api/auth'

  defineOptions({ name: 'CodeLogin' })

  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)
  const router = useRouter()
  const route = useRoute()

  // 访问控制：控制各登录方式/第三方渠道卡片显隐
  const emailEnabled = ref(true)
  const thirdPartyChannels = ref<string[]>([])
  const isChannelEnabled = (channel: string) => thirdPartyChannels.value.includes(channel)

  onMounted(async () => {
    try {
      const config = await fetchGetAccessConfig()
      emailEnabled.value = config.loginMethods?.includes('email_code') ?? false
      thirdPartyChannels.value = config.thirdPartyLoginChannels ?? []
    } catch {
      // 拉取失败时保持默认（邮箱可用、第三方隐藏），后端仍会拒绝被关闭的方式
      thirdPartyChannels.value = []
    }
  })

  const emailIcon = computed(() => (isDark.value ? gmailCleanerLightIcon : gmailCleanerDarkIcon))
  const githubIcon = computed(() => (isDark.value ? githubLightIcon : githubDarkIcon))

  const selectChannel = (next: Api.Auth.LoginCodeChannel) => {
    grantAuthRouteAccess('CodeLoginVerify')
    router.push({
      name: 'CodeLoginVerify',
      query: { channel: next, ...(route.query.redirect ? { redirect: route.query.redirect } : {}) }
    })
  }
</script>

<style scoped>
  @import '../login/style.css';
</style>
