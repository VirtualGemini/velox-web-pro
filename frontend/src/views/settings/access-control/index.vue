<template>
  <div class="velox-full-height access-control-page">
    <ElCard class="velox-table-card">
      <div class="page-header">
        <h2 class="page-title">{{ t('pages.settings.accessControl.title') }}</h2>
        <p class="page-desc">{{ t('pages.settings.accessControl.description') }}</p>
      </div>

      <ElForm v-loading="loading" label-position="top" class="access-control-form">
        <!-- 通用注册 -->
        <div class="ac-section">
          <div class="ac-section__head">
            <div class="ac-section__title">
              {{ t('pages.settings.accessControl.sections.generalRegister.title') }}
            </div>
            <ElSwitch
              v-model="form.generalRegisterEnabled"
              :disabled="!hasAuth('settings:access-control:general-register') || loading"
              @change="handleToggleGeneralRegister"
            />
          </div>
          <div class="ac-section__desc">
            {{ t('pages.settings.accessControl.sections.generalRegister.description') }}
          </div>
        </div>

        <!-- 忘记密码 -->
        <div class="ac-section">
          <div class="ac-section__head">
            <div class="ac-section__title">
              {{ t('pages.settings.accessControl.sections.forgotPassword.title') }}
            </div>
            <ElSwitch
              v-model="form.forgotPasswordEnabled"
              :disabled="!hasAuth('settings:access-control:forgot-password') || loading"
              @change="handleToggleForgotPassword"
            />
          </div>
          <div class="ac-section__desc">
            {{ t('pages.settings.accessControl.sections.forgotPassword.description') }}
          </div>
        </div>

        <!-- 普通登录方式 -->
        <div class="ac-section">
          <div class="ac-section__title">
            {{ t('pages.settings.accessControl.sections.loginMethod.title') }}
          </div>
          <div class="ac-section__desc">
            {{ t('pages.settings.accessControl.sections.loginMethod.description') }}
          </div>
          <ElCheckboxGroup
            v-model="form.loginMethods"
            :disabled="!hasAuth('settings:access-control:login-method') || loading"
            @change="handleUpdateLoginMethods"
          >
            <ElCheckbox
              v-for="m in LOGIN_METHODS"
              :key="m"
              :value="m"
              :label="m"
            >
              {{ t(`pages.settings.accessControl.methods.${m}`) }}
            </ElCheckbox>
          </ElCheckboxGroup>
        </div>

        <!-- 第三方登录方式 -->
        <div class="ac-section">
          <div class="ac-section__title">
            {{ t('pages.settings.accessControl.sections.thirdPartyLogin.title') }}
          </div>
          <div class="ac-section__desc">
            {{ t('pages.settings.accessControl.sections.thirdPartyLogin.description') }}
          </div>
          <ElCheckboxGroup
            v-model="form.thirdPartyLoginChannels"
            :disabled="!hasAuth('settings:access-control:third-party-login') || loading"
            @change="handleUpdateThirdPartyLogin"
          >
            <ElCheckbox
              v-for="c in THIRD_PARTY_CHANNELS"
              :key="c"
              :value="c"
              :label="c"
            >
              {{ t(`pages.settings.accessControl.channels.${c}`) }}
            </ElCheckbox>
          </ElCheckboxGroup>
        </div>

        <!-- 第三方注册方式 -->
        <div class="ac-section">
          <div class="ac-section__title">
            {{ t('pages.settings.accessControl.sections.thirdPartyRegister.title') }}
          </div>
          <div class="ac-section__desc">
            {{ t('pages.settings.accessControl.sections.thirdPartyRegister.description') }}
          </div>
          <ElCheckboxGroup
            v-model="form.thirdPartyRegisterChannels"
            :disabled="!hasAuth('settings:access-control:third-party-register') || loading"
            @change="handleUpdateThirdPartyRegister"
          >
            <ElCheckbox
              v-for="c in THIRD_PARTY_CHANNELS"
              :key="c"
              :value="c"
              :label="c"
            >
              {{ t(`pages.settings.accessControl.channels.${c}`) }}
            </ElCheckbox>
          </ElCheckboxGroup>
        </div>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage } from 'element-plus'
  import { useAuth } from '@/hooks/core/useAuth'
  import {
    fetchGetAccessControl,
    fetchUpdateForgotPassword,
    fetchUpdateGeneralRegister,
    fetchUpdateLoginMethods,
    fetchUpdateThirdPartyLogin,
    fetchUpdateThirdPartyRegister,
    type AccessControlConfig
  } from '@/api/settings'

  defineOptions({ name: 'AccessControl' })

  const { t } = useI18n()
  const { hasAuth } = useAuth()

  const LOGIN_METHODS = ['password', 'email_code']
  const THIRD_PARTY_CHANNELS = ['github', 'linuxdo']

  const loading = ref(false)

  const form = reactive<AccessControlConfig>({
    generalRegisterEnabled: true,
    forgotPasswordEnabled: true,
    loginMethods: [],
    thirdPartyLoginChannels: [],
    thirdPartyRegisterChannels: []
  })

  // 保存上一次成功的快照，便于请求失败时回滚 UI 状态
  const snapshot = reactive<AccessControlConfig>({
    generalRegisterEnabled: true,
    forgotPasswordEnabled: true,
    loginMethods: [],
    thirdPartyLoginChannels: [],
    thirdPartyRegisterChannels: []
  })

  function applyToForm(data: AccessControlConfig) {
    form.generalRegisterEnabled = data.generalRegisterEnabled
    form.forgotPasswordEnabled = data.forgotPasswordEnabled
    form.loginMethods = [...(data.loginMethods ?? [])]
    form.thirdPartyLoginChannels = [...(data.thirdPartyLoginChannels ?? [])]
    form.thirdPartyRegisterChannels = [...(data.thirdPartyRegisterChannels ?? [])]
    saveSnapshot()
  }

  function saveSnapshot() {
    snapshot.generalRegisterEnabled = form.generalRegisterEnabled
    snapshot.forgotPasswordEnabled = form.forgotPasswordEnabled
    snapshot.loginMethods = [...form.loginMethods]
    snapshot.thirdPartyLoginChannels = [...form.thirdPartyLoginChannels]
    snapshot.thirdPartyRegisterChannels = [...form.thirdPartyRegisterChannels]
  }

  type FieldKey = keyof AccessControlConfig

  function cloneVal<T>(v: T): T {
    return (Array.isArray(v) ? [...v] : v) as T
  }

  // 逐字段提交/回滚：单个开关更新失败只回滚该字段，
  // 避免并发更新不同字段时相互覆盖，也无需整表 loading 遮罩
  function commitField(key: FieldKey) {
    ;(snapshot as Record<FieldKey, unknown>)[key] = cloneVal(form[key])
  }

  function revertField(key: FieldKey) {
    ;(form as Record<FieldKey, unknown>)[key] = cloneVal(snapshot[key])
  }

  async function loadConfig() {
    loading.value = true
    try {
      const data = await fetchGetAccessControl()
      applyToForm(data)
    } catch {
      ElMessage.error(t('pages.settings.accessControl.loadFailed'))
    } finally {
      loading.value = false
    }
  }

  // 不再切换整表 loading：开关/复选框本身已乐观反映新状态，
  // 成功后提交快照，失败则回滚该字段（错误提示由 http 拦截器统一弹出）
  async function runUpdate(key: FieldKey, action: () => Promise<unknown>) {
    try {
      await action()
      commitField(key)
      ElMessage.success(t('pages.settings.accessControl.saveSuccess'))
    } catch {
      revertField(key)
    }
  }

  function handleToggleGeneralRegister(val: string | number | boolean) {
    runUpdate('generalRegisterEnabled', () => fetchUpdateGeneralRegister(Boolean(val)))
  }

  function handleToggleForgotPassword(val: string | number | boolean) {
    runUpdate('forgotPasswordEnabled', () => fetchUpdateForgotPassword(Boolean(val)))
  }

  function handleUpdateLoginMethods() {
    if (form.loginMethods.length === 0) {
      revertField('loginMethods')
      ElMessage.warning(t('pages.settings.accessControl.sections.loginMethod.atLeastOne'))
      return
    }
    runUpdate('loginMethods', () => fetchUpdateLoginMethods([...form.loginMethods]))
  }

  function handleUpdateThirdPartyLogin() {
    runUpdate('thirdPartyLoginChannels', () =>
      fetchUpdateThirdPartyLogin([...form.thirdPartyLoginChannels])
    )
  }

  function handleUpdateThirdPartyRegister() {
    runUpdate('thirdPartyRegisterChannels', () =>
      fetchUpdateThirdPartyRegister([...form.thirdPartyRegisterChannels])
    )
  }

  onMounted(loadConfig)
</script>

<style lang="scss" scoped>
  .access-control-page {
    .page-header {
      margin-bottom: 20px;

      .page-title {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: 600;
      }

      .page-desc {
        margin: 0;
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }
    }

    .ac-section {
      padding: 18px 0;
      border-bottom: 1px solid var(--el-border-color-lighter);

      &:last-child {
        border-bottom: none;
      }

      &__head {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      &__title {
        font-size: 15px;
        font-weight: 500;
      }

      &__desc {
        margin: 6px 0 12px;
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }
    }
  }
</style>
