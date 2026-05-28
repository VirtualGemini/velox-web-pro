<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useSecurityStatus } from '../../composables/useSecurityStatus'
  import RebindEmailDialog from './dialogs/RebindEmailDialog.vue'
  import UnbindEmailDialog from './dialogs/UnbindEmailDialog.vue'
  import EnableMfaEmailDialog from './dialogs/EnableMfaEmailDialog.vue'
  import EnableMfaTotpDialog from './dialogs/EnableMfaTotpDialog.vue'
  import DisableMfaTotpDialog from './dialogs/DisableMfaTotpDialog.vue'
  import ResetPasswordDialog from './dialogs/ResetPasswordDialog.vue'
  import { fetchUpdateLoginMethods } from '@/api/security'

  defineProps<{
    detail: Partial<Api.Auth.AccountDetail>
  }>()

  const emit = defineEmits<{
    (e: 'detail-updated', patch: Partial<Api.Auth.AccountDetail>): void
    (e: 'account-tab-updated', patch: Partial<Api.Auth.AccountTabInfo>): void
  }>()

  const { t } = useI18n()
  const { status, loading, load, refresh } = useSecurityStatus()

  const rebindEmailVisible = ref(false)
  const unbindEmailVisible = ref(false)
  const resetPasswordVisible = ref(false)
  const mfaDialogVisible = ref(false)
  const mfaTargetEnabled = ref(false)
  const mfaTotpEnableVisible = ref(false)
  const mfaTotpDisableVisible = ref(false)

  load()

  const loginMethodKeys = ['password', 'email_code'] as const

  const isMethodEnabled = (key: string) => status.value?.loginMethods?.includes(key) ?? false
  const isMethodAllowed = (key: string) => status.value?.allowedLoginMethods?.includes(key) ?? false

  const emailMfaOn = computed(() => status.value?.mfa?.email ?? false)
  const totpMfaOn = computed(() => status.value?.mfa?.totp ?? false)

  const onMethodChange = async (key: string, value: boolean) => {
    if (!status.value) return
    const next = new Set(status.value.loginMethods || [])
    if (value) next.add(key)
    else next.delete(key)

    if (next.size === 0) {
      ElMessage.warning(t('pages.system.accountCenter.security.loginMethods.atLeastOne'))
      return refresh()
    }
    try {
      await fetchUpdateLoginMethods({ methods: Array.from(next) })
      ElMessage.success(t('pages.system.accountCenter.security.loginMethods.saveSuccess'))
      await refresh()
    } catch {
      await refresh()
    }
  }

  const onMfaEmailSwitchClick = () => {
    if (totpMfaOn.value) {
      ElMessage.warning(t('pages.system.accountCenter.security.mfa.mutualExclusive'))
      return
    }
    if (!status.value?.email) {
      ElMessage.warning(t('pages.system.accountCenter.security.mfa.email.noEmail'))
      return
    }
    mfaTargetEnabled.value = !emailMfaOn.value
    mfaDialogVisible.value = true
  }

  const onMfaTotpSwitchChange = (val: boolean) => {
    if (val) {
      if (emailMfaOn.value) {
        ElMessage.warning(t('pages.system.accountCenter.security.mfa.mutualExclusive'))
        refresh()
        return
      }
      mfaTotpEnableVisible.value = true
    } else {
      mfaTotpDisableVisible.value = true
    }
  }

  const onEmailRebound = (newEmail: string) => {
    emit('detail-updated', { securityEmail: newEmail })
    emit('account-tab-updated', { securityEmail: newEmail, emailMfaEnabled: false })
    refresh()
  }

  const onEmailUnbound = () => {
    emit('detail-updated', { securityEmail: '' })
    emit('account-tab-updated', { securityEmail: '', emailMfaEnabled: false })
    refresh()
  }

  const onMfaEmailEnabled = () => {
    emit('account-tab-updated', { emailMfaEnabled: mfaTargetEnabled.value })
    refresh()
  }

  const onMfaTotpEnabled = () => {
    emit('account-tab-updated', { totpMfaEnabled: true, emailMfaEnabled: false })
    refresh()
  }

  const onMfaTotpDisabled = () => {
    emit('account-tab-updated', { totpMfaEnabled: false })
    refresh()
  }
</script>

<template>
  <div class="github-security-settings">
    <header class="border-b border-g-300 pb-3">
      <h1 class="text-xl font-medium leading-8 text-g-900">
        {{ t('pages.system.accountCenter.security.title') }}
      </h1>
      <p class="mt-1 text-sm text-g-600">
        {{ t('pages.system.accountCenter.security.desc') }}
      </p>
    </header>

    <div v-loading="loading">
      <section class="setting-section">
        <div class="setting-main">
          <h2 class="setting-title">
            {{ t('pages.system.accountCenter.security.passwordReset.entryTitle') }}
          </h2>
          <p class="setting-desc">
            {{ t('pages.system.accountCenter.security.passwordReset.entryDesc') }}
          </p>
          <div v-if="status?.lastPasswordChangeAt" class="mt-2 text-sm text-g-600">
            {{
              t('pages.system.accountCenter.security.passwordReset.lastChangedAt', {
                time: status.lastPasswordChangeAt
              })
            }}
          </div>
        </div>
        <div class="setting-action">
          <ElButton @click="resetPasswordVisible = true">
            {{ t('pages.system.accountCenter.security.passwordReset.action') }}
          </ElButton>
        </div>
      </section>

      <section class="setting-section">
        <div class="setting-main">
          <h2 class="setting-title">
            {{ t('pages.system.accountCenter.security.email.title') }}
          </h2>
          <p class="setting-desc">
            {{ t('pages.system.accountCenter.security.email.desc') }}
          </p>
          <div class="mt-2 text-sm">
            {{ detail.securityEmail || status?.email || t('pages.system.accountCenter.security.email.unbound') }}
          </div>
        </div>
        <div class="setting-action security-email-action">
          <ElSpace>
            <ElButton @click="rebindEmailVisible = true">
              {{
                status?.email
                  ? t('pages.system.accountCenter.security.email.rebind')
                  : t('pages.system.accountCenter.security.email.bind')
              }}
            </ElButton>
          </ElSpace>
          <div v-if="status?.email" class="security-email-unbind-row">
            <ElButton @click="unbindEmailVisible = true">
              {{ t('pages.system.accountCenter.security.email.unbind') }}
            </ElButton>
          </div>
        </div>
      </section>

      <section class="setting-section block-section">
        <div class="mb-4">
          <h2 class="setting-title">
            {{ t('pages.system.accountCenter.security.loginMethods.title') }}
          </h2>
          <p class="setting-desc">
            {{ t('pages.system.accountCenter.security.loginMethods.desc') }}
          </p>
        </div>

        <div class="setting-list">
          <div v-for="key in loginMethodKeys" :key="key" class="setting-row">
            <div class="setting-main">
              <div class="text-sm font-medium">
                {{ t(`pages.system.accountCenter.security.loginMethods.items.${key}`) }}
              </div>
              <div v-if="!isMethodAllowed(key)" class="mt-1 text-xs text-g-500">
                {{ t('pages.system.accountCenter.security.loginMethods.globalDisabled') }}
              </div>
            </div>
            <ElSwitch
              :model-value="isMethodEnabled(key)"
              :disabled="!isMethodAllowed(key)"
              @change="(val: string | number | boolean) => onMethodChange(key, Boolean(val))"
            />
          </div>
        </div>
      </section>

      <section class="setting-section block-section">
        <div class="mb-4">
          <h2 class="setting-title">
            {{ t('pages.system.accountCenter.security.mfa.title') }}
          </h2>
          <p class="setting-desc">
            {{ t('pages.system.accountCenter.security.mfa.desc') }}
          </p>
        </div>

        <div class="setting-list">
          <div class="setting-row">
            <div class="setting-main">
              <div class="flex items-center gap-2 text-sm font-medium">
                {{ t('pages.system.accountCenter.security.mfa.items.totp') }}
                <span class="px-1.5 py-0.5 text-[10px] rounded bg-theme/10 text-theme">
                  {{ t('pages.system.accountCenter.security.mfa.recommended') }}
                </span>
                <span v-if="totpMfaOn" class="ml-1 text-xs text-green-600">
                  {{ t('pages.system.accountCenter.security.mfa.enabled') }}
                </span>
              </div>
              <div class="mt-1 text-xs text-g-500">
                {{ t('pages.system.accountCenter.security.mfa.totp.desc') }}
              </div>
            </div>
            <ElSwitch
              :model-value="totpMfaOn"
              @click.stop
              @change="(val: string | number | boolean) => onMfaTotpSwitchChange(Boolean(val))"
            />
          </div>

          <div class="setting-row">
            <div class="setting-main">
              <div class="text-sm font-medium">
                {{ t('pages.system.accountCenter.security.mfa.items.email') }}
                <span v-if="emailMfaOn" class="ml-2 text-xs text-green-600">
                  {{ t('pages.system.accountCenter.security.mfa.enabled') }}
                </span>
              </div>
              <div class="mt-1 text-xs text-g-500">
                {{ t('pages.system.accountCenter.security.mfa.email.hint') }}
              </div>
            </div>
            <ElSwitch
              :model-value="emailMfaOn"
              :disabled="totpMfaOn"
              @click.stop
              @change="
                (val: string | number | boolean) => {
                  if (val) onMfaEmailSwitchClick()
                  else {
                    mfaTargetEnabled = false
                    mfaDialogVisible = true
                  }
                }
              "
            />
          </div>
        </div>
      </section>
    </div>

    <RebindEmailDialog
      v-model:visible="rebindEmailVisible"
      :current-email="status?.email"
      :totp-enabled="status?.mfa?.totp ?? false"
      @success="onEmailRebound"
    />
    <UnbindEmailDialog
      v-model:visible="unbindEmailVisible"
      :email="status?.email"
      :totp-enabled="status?.mfa?.totp ?? false"
      @success="onEmailUnbound"
    />
    <ResetPasswordDialog
      v-model:visible="resetPasswordVisible"
      :email-mfa-enabled="status?.mfa?.email ?? false"
      :totp-mfa-enabled="status?.mfa?.totp ?? false"
      @success="refresh"
    />
    <EnableMfaEmailDialog
      v-model:visible="mfaDialogVisible"
      :enable="mfaTargetEnabled"
      @success="onMfaEmailEnabled"
    />
    <EnableMfaTotpDialog v-model:visible="mfaTotpEnableVisible" @success="onMfaTotpEnabled" />
    <DisableMfaTotpDialog v-model:visible="mfaTotpDisableVisible" @success="onMfaTotpDisabled" />
  </div>
</template>

<style scoped>
  .setting-section {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 24px;
    border-bottom: 1px solid var(--velox-gray-300);
    padding: 24px 0;
  }

  .block-section {
    display: block;
  }

  .setting-title {
    color: var(--velox-text-color);
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
  }

  .setting-desc {
    margin-top: 4px;
    color: var(--velox-gray-600);
    font-size: 13px;
    line-height: 20px;
  }

  .setting-action {
    flex: 0 0 auto;
    margin-left: auto;
  }

  .security-email-action {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
  }

  .security-email-unbind-row {
    margin-top: 8px;
  }

  .setting-list {
    width: 100%;
  }

  .setting-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
    padding: 14px 0;
  }

  .github-security-settings {
    padding-bottom: 80px;
  }

  @media only screen and (width <= 640px) {
    .setting-section {
      display: block;
    }

    .setting-action {
      margin-top: 12px;
      margin-left: 0;
    }
  }
</style>
