<!-- 登录页面 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <template v-if="showLoggedInCard">
            <h3 class="title">{{ $t('login.loggedIn.title') }}</h3>
            <p class="sub-title">{{ $t('login.loggedIn.subTitle') }}</p>
            <div style="margin-top: 25px">
              <VeloxLoggedInCard
                :user-info="userInfo"
                :selected="accountSelected"
                @select="accountSelected = !accountSelected"
                @logout="handleLogout"
              />
            </div>
            <div style="margin-top: 30px">
              <ElButton
                class="w-full custom-height"
                type="primary"
                @click="handleContinue"
                v-ripple
              >
                {{ $t('login.loggedIn.continueBtn') }}
              </ElButton>
            </div>
          </template>
          <template v-else>
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
                <RouterLink class="text-theme" :to="{ name: 'ForgetPassword' }">{{
                  $t('login.forgetPwd')
                }}</RouterLink>
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

              <div class="mt-5 text-sm text-gray-600">
                <span>{{ $t('login.noAccount') }}</span>
                <RouterLink class="text-theme" :to="{ name: 'Register' }">{{
                  $t('login.register')
                }}</RouterLink>
              </div>
            </ElForm>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import AppConfig from '@/config'
  import { useUserStore } from '@/store/modules/user'
  import { useI18n } from 'vue-i18n'
  import { HttpError } from '@/utils/http/error'
  import { fetchLogin } from '@/api/auth'
  import {
    ElMessage,
    ElMessageBox,
    ElNotification,
    type FormInstance,
    type FormRules
  } from 'element-plus'
  import { useSettingStore } from '@/store/modules/setting'

  defineOptions({ name: 'Login' })

  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)
  const { t, locale } = useI18n()
  const formKey = ref(0)

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

  const { getUserInfo: userInfo } = storeToRefs(userStore)
  // 进入登录页时一次性快照：仅在挂载时已登录的情况下展示账号卡片
  // 这样表单登录成功后 isLogin 翻转为 true 也不会在跳转前闪现卡片
  const showLoggedInCard = ref(userStore.isLogin && !!userStore.accessToken)
  const accountSelected = ref(false)

  const systemName = AppConfig.systemInfo.name
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

  // 继续使用当前已登录账号
  const handleContinue = () => {
    if (!accountSelected.value) {
      ElMessage.warning(t('login.loggedIn.selectTip'))
      return
    }
    const redirect = route.query.redirect as string | undefined
    router.push(redirect || '/')
  }

  // 退出当前账号，回到登录表单状态
  const handleLogout = () => {
    ElMessageBox.confirm(t('common.logOutTips'), t('common.tips'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      customClass: 'login-out-dialog'
    })
      .then(() => {
        showLoggedInCard.value = false
        userStore.logOut()
      })
      .catch(() => {
        // 用户取消，保持卡片显示
      })
  }

  // 登录
  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      // 表单验证
      const valid = await formRef.value.validate()
      if (!valid) return

      // 拖拽验证
      if (!isPassing.value) {
        isClickPass.value = true
        return
      }

      loading.value = true

      // 登录请求
      const { username, password } = formData

      const { token, refreshToken } = await fetchLogin({
        userName: username,
        password
      })

      // 验证token
      if (!token) {
        throw new Error('Login failed - no token received')
      }

      // 存储 token 和登录状态
      userStore.setToken(token, refreshToken)
      userStore.setLoginStatus(true)

      // 登录成功处理
      showLoginSuccessNotice()

      // 获取 redirect 参数，如果存在则跳转到指定页面，否则跳转到首页
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error) {
      // 处理 HttpError
      if (error instanceof HttpError) {
        // console.log(error.code)
      } else {
        // 处理非 HttpError
        // ElMessage.error('登录失败，请稍后重试')
        console.error('[Login] Unexpected error:', error)
      }
    } finally {
      loading.value = false
      resetDragVerify()
    }
  }

  // 重置拖拽验证
  const resetDragVerify = () => {
    dragVerify.value.reset()
  }

  // 登录成功提示
  const showLoginSuccessNotice = () => {
    setTimeout(() => {
      ElNotification({
        title: t('login.success.title'),
        type: 'success',
        duration: 2500,
        zIndex: 10000,
        message: `${t('login.success.message')}, ${systemName}!`
      })
    }, 1000)
  }
</script>

<style scoped>
  @import './style.css';
</style>
