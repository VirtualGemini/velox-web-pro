<!-- 登录页面 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <AuthLoggedInGuard>
            <h3 class="title">{{ $t('login.title') }}</h3>
            <p class="sub-title">{{ $t('login.subTitle') }}</p>
            <ElForm
              ref="formRef"
              :model="formData"
              :rules="rules"
              :key="formKey"
              @keyup.enter="handleSubmit"
              style="margin-top: 25px"
            >
              <ElFormItem prop="username">
                <ElInput
                  class="custom-height"
                  :placeholder="$t('login.placeholder.username')"
                  v-model.trim="formData.username"
                />
              </ElFormItem>
              <ElFormItem prop="password">
                <ElInput
                  class="custom-height"
                  :placeholder="$t('login.placeholder.password')"
                  v-model.trim="formData.password"
                  type="password"
                  autocomplete="off"
                  show-password
                />
              </ElFormItem>

              <!-- 推拽验证 -->
              <div class="relative pb-5 mt-6">
                <div
                  class="relative z-[2] overflow-hidden select-none rounded-lg border border-transparent tad-300"
                  :class="{ '!border-[#FF4E4F]': !isPassing && isClickPass }"
                >
                  <VeloxDragVerify
                    ref="dragVerify"
                    v-model:value="isPassing"
                    :text="$t('login.sliderText')"
                    textColor="var(--velox-gray-700)"
                    :successText="$t('login.sliderSuccessText')"
                    progressBarBg="var(--main-color)"
                    :background="isDark ? '#26272F' : '#F1F1F4'"
                    handlerBg="var(--default-box-color)"
                  />
                </div>
                <p
                  class="absolute top-0 z-[1] px-px mt-2 text-xs text-[#f56c6c] tad-300"
                  :class="{ 'translate-y-10': !isPassing && isClickPass }"
                >
                  {{ $t('login.placeholder.slider') }}
                </p>
              </div>

              <div class="flex-cb mt-2 text-sm">
                <ElCheckbox v-model="formData.rememberPassword">{{
                  $t('login.rememberPwd')
                }}</ElCheckbox>
                <RouterLink
                  v-if="forgotPasswordEnabled"
                  class="text-theme"
                  :to="{ name: 'ForgetPassword' }"
                  >{{ $t('login.forgetPwd') }}</RouterLink
                >
              </div>

              <div style="margin-top: 30px">
                <ElButton
                  class="w-full custom-height"
                  type="primary"
                  @click="handleSubmit"
                  :loading="loading"
                  v-ripple
                >
                  {{ $t('login.btnText') }}
                </ElButton>
              </div>

              <div
                v-if="generalRegisterEnabled || otherLoginEnabled"
                class="mt-5 flex-cb text-sm text-gray-600"
              >
                <div v-if="generalRegisterEnabled">
                  <span>{{ $t('login.noAccount') }}</span>
                  <RouterLink class="text-theme" :to="{ name: 'Register' }">{{
                    $t('login.register')
                  }}</RouterLink>
                </div>
                <RouterLink
                  v-if="otherLoginEnabled"
                  class="text-theme"
                  :to="{ name: 'CodeLogin' }"
                  >{{ $t('login.otherLogin') }}</RouterLink
                >
              </div>
            </ElForm>
          </AuthLoggedInGuard>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { useI18n } from 'vue-i18n'
  import { HttpError } from '@/utils/http/error'
  import { logger } from '@/utils/sys/logger'
  import { fetchLogin, fetchGetAccessConfig } from '@/api/auth'
  import type { FormInstance, FormRules } from 'element-plus'
  import { useSettingStore } from '@/store/modules/setting'
  import { completeLogin } from '../shared/completeLogin'
  import { grantAuthRouteAccess } from '../shared/routeAccess'

  defineOptions({ name: 'Login' })

  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)
  const { t, locale } = useI18n()
  const formKey = ref(0)

  // 访问控制：决定登录页展示哪些入口（密码表单始终显示）
  const generalRegisterEnabled = ref(false)
  const forgotPasswordEnabled = ref(false)
  const otherLoginEnabled = ref(false)

  const loadAccessConfig = async () => {
    try {
      const config = await fetchGetAccessConfig()
      generalRegisterEnabled.value = config.generalRegisterEnabled
      forgotPasswordEnabled.value = config.forgotPasswordEnabled
      // “使用其它方式登录”入口：邮箱验证码或任一第三方登录渠道开启时显示
      otherLoginEnabled.value =
        (config.loginMethods?.includes('email_code') ?? false) ||
        (config.thirdPartyLoginChannels?.length ?? 0) > 0
    } catch {
      // 拉取失败时保持入口隐藏（最小暴露），不阻塞密码登录
    }
  }

  onMounted(loadAccessConfig)

  // 监听语言切换，重置表单
  watch(locale, () => {
    formKey.value++
  })

  const dragVerify = ref()

  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  const isPassing = ref(false)
  const isClickPass = ref(false)

  const formRef = ref<FormInstance>()

  const formData = reactive({
    username: '',
    password: '',
    rememberPassword: true
  })

  const rules = computed<FormRules>(() => ({
    username: [{ required: true, message: t('login.placeholder.username'), trigger: 'blur' }],
    password: [{ required: true, message: t('login.placeholder.password'), trigger: 'blur' }]
  }))

  const loading = ref(false)

  // 登录
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      // 表单验证（失败不抛出，避免校验对象进入下方 catch）
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return

      // 拖拽验证
      if (!isPassing.value) {
        isClickPass.value = true
        return
      }

      loading.value = true

      // 登录请求
      const { username, password } = formData

      const response = await fetchLogin({
        userName: username,
        password
      })

      const { token, refreshToken, mfaChallenge, mfaType, mfaEmailMasked, mfaTotpDigits } = response

      if (mfaChallenge) {
        grantAuthRouteAccess('MfaChallenge')
        router.push({
          name: 'MfaChallenge',
          query: {
            challenge: mfaChallenge,
            mfaType: mfaType || undefined,
            target: mfaEmailMasked || undefined,
            mfaTotpDigits: mfaTotpDigits ? String(mfaTotpDigits) : undefined,
            redirect: route.query.redirect as string | undefined
          }
        })
        return
      }

      if (response.pendingDeletion) {
        if (response.token) {
          userStore.setToken(response.token, response.refreshToken)
        }
        userStore.setPendingDeletionLogin(response)
        userStore.setAccountInfo({
          accountId: response.accountId || '',
          userName: response.userName || '',
          email: response.email || '',
          avatar: response.avatar || '',
          roles: [],
          buttons: []
        })
        userStore.setLoginStatus(false)
        return
      }

      // 验证token
      if (!token) {
        throw new Error('Login failed - no token received')
      }

      await completeLogin({
        accountStore: userStore,
        token,
        refreshToken,
        redirect: route.query.redirect as string | undefined,
        router,
        successTitle: t('login.success.title'),
        successMessage: t('login.success.message')
      })
    } catch (error) {
      // HttpError 已由 HTTP 层统一提示与记录；此处仅兜底记录非预期的代码异常
      if (!(error instanceof HttpError)) {
        logger.error('[Login] Unexpected error:', error)
      }
    } finally {
      loading.value = false
      resetDragVerify()
    }
  }

  // 重置拖拽验证
  const resetDragVerify = () => {
    dragVerify.value?.reset()
  }
</script>

<style scoped>
  @import './style.css';
</style>
