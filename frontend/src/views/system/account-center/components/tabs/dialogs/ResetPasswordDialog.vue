<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchSendMfaEmailCode } from '@/api/security'
  import { fetchUpdateAccountPassword } from '@/api/auth'

  type MfaType = 'email' | 'totp'

  const props = defineProps<{
    visible: boolean
    emailMfaEnabled: boolean
    totpMfaEnabled: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  const { t } = useI18n()
  const passwordFormRef = ref<FormInstance>()
  const mfaFormRef = ref<FormInstance>()
  const activeTab = ref<'password' | 'mfa'>('password')
  const mfaType = ref<MfaType>('totp')
  const cooldown = ref(0)
  const sendingCode = ref(false)
  const submitting = ref(false)

  const passwordForm = reactive({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  const mfaForm = reactive({
    emailCode: '',
    totpCode: ''
  })

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const hasMfa = computed(() => props.emailMfaEnabled || props.totpMfaEnabled)
  const mfaOptions = computed(() => {
    const options: Array<{ value: MfaType; label: string }> = []
    if (props.totpMfaEnabled) {
      options.push({
        value: 'totp',
        label: t('pages.system.accountCenter.security.passwordReset.mfa.totp')
      })
    }
    if (props.emailMfaEnabled) {
      options.push({
        value: 'email',
        label: t('pages.system.accountCenter.security.passwordReset.mfa.email')
      })
    }
    return options
  })

  const passwordRules: FormRules = {
    currentPassword: [
      {
        required: true,
        message: t('pages.system.accountCenter.password.validation.currentPasswordRequired'),
        trigger: 'blur'
      }
    ],
    newPassword: [
      {
        required: true,
        message: t('pages.system.accountCenter.password.validation.newPasswordRequired'),
        trigger: 'blur'
      },
      {
        min: 6,
        max: 32,
        message: t('pages.system.accountCenter.password.validation.passwordLength'),
        trigger: 'blur'
      }
    ],
    confirmPassword: [
      {
        required: true,
        message: t('pages.system.accountCenter.password.validation.confirmPasswordRequired'),
        trigger: 'blur'
      },
      {
        validator: (_rule, value, callback) => {
          if (value !== passwordForm.newPassword) {
            callback(new Error(t('pages.system.accountCenter.password.validation.passwordMismatch')))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }

  const mfaRules = computed<FormRules>(() => ({
    emailCode: [
      {
        required: hasMfa.value && mfaType.value === 'email',
        message: t('pages.system.accountCenter.security.passwordReset.validation.emailCodeRequired'),
        trigger: 'blur'
      }
    ],
    totpCode: [
      {
        required: hasMfa.value && mfaType.value === 'totp',
        message: t('pages.system.accountCenter.security.passwordReset.validation.totpCodeRequired'),
        trigger: 'blur'
      }
    ]
  }))

  let cooldownTimer: number | undefined

  const clearCooldownTimer = () => {
    if (cooldownTimer) {
      window.clearInterval(cooldownTimer)
      cooldownTimer = undefined
    }
  }

  const startCooldown = () => {
    cooldown.value = 60
    clearCooldownTimer()
    cooldownTimer = window.setInterval(() => {
      cooldown.value -= 1
      if (cooldown.value <= 0) {
        clearCooldownTimer()
      }
    }, 1000)
  }

  const resetForms = () => {
    activeTab.value = 'password'
    mfaType.value = props.totpMfaEnabled ? 'totp' : 'email'
    cooldown.value = 0
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    mfaForm.emailCode = ''
    mfaForm.totpCode = ''
    passwordFormRef.value?.clearValidate()
    mfaFormRef.value?.clearValidate()
    clearCooldownTimer()
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        resetForms()
        return
      }
      resetForms()
    }
  )

  const sendEmailCode = async () => {
    sendingCode.value = true
    try {
      await fetchSendMfaEmailCode()
      ElMessage.success(t('pages.system.accountCenter.security.mfa.email.codeSent'))
      startCooldown()
    } finally {
      sendingCode.value = false
    }
  }

  const submit = async () => {
    const passwordValid = await passwordFormRef.value?.validate().catch(() => false)
    if (!passwordValid) {
      activeTab.value = 'password'
      return
    }

    let mfaValid = true
    if (hasMfa.value) {
      mfaValid = Boolean(await mfaFormRef.value?.validate().catch(() => false))
      if (!mfaValid) {
        activeTab.value = 'mfa'
        return
      }
    }

    submitting.value = true
    try {
      await fetchUpdateAccountPassword({
        currentPassword: passwordForm.currentPassword.trim(),
        newPassword: passwordForm.newPassword.trim(),
        confirmPassword: passwordForm.confirmPassword.trim(),
        mfaType: hasMfa.value ? mfaType.value : undefined,
        mfaEmailCode:
          hasMfa.value && mfaType.value === 'email' ? mfaForm.emailCode.trim() : undefined,
        mfaTotpCode: hasMfa.value && mfaType.value === 'totp' ? mfaForm.totpCode.trim() : undefined
      })
      ElMessage.success(t('pages.system.accountCenter.messages.passwordUpdateSuccess'))
      emit('success')
      dialogVisible.value = false
    } finally {
      submitting.value = false
    }
  }
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    :title="t('pages.system.accountCenter.security.passwordReset.title')"
    width="520px"
    align-center
    :close-on-click-modal="false"
    :lock-scroll="false"
  >
    <ElTabs v-if="hasMfa" v-model="activeTab" class="mb-4">
      <ElTabPane
        :label="t('pages.system.accountCenter.security.passwordReset.tabs.password')"
        name="password"
      />
      <ElTabPane :label="t('pages.system.accountCenter.security.passwordReset.tabs.mfa')" name="mfa" />
    </ElTabs>

    <ElForm
      v-show="activeTab === 'password'"
      ref="passwordFormRef"
      :model="passwordForm"
      :rules="passwordRules"
      label-position="top"
    >
      <ElFormItem
        :label="t('pages.system.accountCenter.password.fields.currentPassword')"
        prop="currentPassword"
      >
        <ElInput v-model="passwordForm.currentPassword" type="password" show-password />
      </ElFormItem>
      <ElFormItem
        :label="t('pages.system.accountCenter.password.fields.newPassword')"
        prop="newPassword"
      >
        <ElInput v-model="passwordForm.newPassword" type="password" show-password />
      </ElFormItem>
      <ElFormItem
        :label="t('pages.system.accountCenter.password.fields.confirmPassword')"
        prop="confirmPassword"
      >
        <ElInput v-model="passwordForm.confirmPassword" type="password" show-password />
      </ElFormItem>
    </ElForm>

    <div v-if="hasMfa" v-show="activeTab === 'mfa'" class="mfa-verification">
      <ElForm ref="mfaFormRef" :model="mfaForm" :rules="mfaRules" label-position="top">
        <ElFormItem
          v-if="mfaOptions.length > 1"
          :label="t('pages.system.accountCenter.security.passwordReset.mfa.type')"
        >
          <ElRadioGroup v-model="mfaType">
            <ElRadioButton v-for="option in mfaOptions" :key="option.value" :label="option.value">
              {{ option.label }}
            </ElRadioButton>
          </ElRadioGroup>
        </ElFormItem>

        <ElFormItem
          v-if="mfaType === 'totp'"
          :label="t('pages.system.accountCenter.security.passwordReset.mfa.totpCode')"
          prop="totpCode"
        >
          <ElInput
            v-model="mfaForm.totpCode"
            :placeholder="t('pages.system.accountCenter.security.passwordReset.mfa.totpPlaceholder')"
            maxlength="6"
          />
        </ElFormItem>

        <ElFormItem
          v-if="mfaType === 'email'"
          :label="t('pages.system.accountCenter.security.passwordReset.mfa.emailCode')"
          prop="emailCode"
        >
          <div class="flex w-full gap-2">
            <ElInput
              v-model="mfaForm.emailCode"
              :placeholder="
                t('pages.system.accountCenter.security.passwordReset.mfa.emailPlaceholder')
              "
              maxlength="6"
            />
            <ElButton
              class="mfa-code-button"
              :disabled="cooldown > 0 || sendingCode"
              :loading="sendingCode"
              @click="sendEmailCode"
            >
              {{
                cooldown > 0
                  ? `${cooldown}s`
                  : t('pages.system.accountCenter.security.passwordReset.mfa.sendEmailCode')
              }}
            </ElButton>
          </div>
        </ElFormItem>
      </ElForm>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton type="primary" :loading="submitting" @click="submit">
        {{ t('common.confirm') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
  .mfa-verification {
    margin-top: 0;
  }

  .mfa-code-button {
    width: 104px;
    flex: 0 0 104px;
  }
</style>
