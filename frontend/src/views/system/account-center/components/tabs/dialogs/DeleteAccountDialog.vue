<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { fetchSendMfaEmailCode } from '@/api/security'
  import { fetchRequestAccountDeletion } from '@/api/auth'
  import { useI18n } from 'vue-i18n'
  import { useAccountStore } from '@/store/modules/user'

  type StepKey = 'verify' | 'confirm'

  const props = defineProps<{
    visible: boolean
    username: string
    securityEmail?: string
    emailMfaEnabled: boolean
    totpMfaEnabled: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  const { t } = useI18n()
  const accountStore = useAccountStore()
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })
  const verifyFormRef = ref<FormInstance>()
  const confirmFormRef = ref<FormInstance>()
  const submitting = ref(false)
  const sendingCode = ref(false)
  const cooldown = ref(0)
  const step = ref<StepKey>('verify')
  const hasSecurityEmail = computed(() => !!props.securityEmail)
  const mfaBlocking = computed(() => props.emailMfaEnabled || props.totpMfaEnabled)
  const form = reactive({
    currentPassword: '',
    emailCode: '',
    username: ''
  })

  const verifyRules = computed<FormRules>(() => ({
    currentPassword: [
      {
        required: !hasSecurityEmail.value,
        message: t('pages.system.accountCenter.account.deletion.validation.currentPasswordRequired'),
        trigger: 'blur'
      }
    ],
    emailCode: [
      {
        required: hasSecurityEmail.value,
        message: t('pages.system.accountCenter.account.deletion.validation.emailCodeRequired'),
        trigger: 'blur'
      }
    ]
  }))

  const confirmRules = computed<FormRules>(() => ({
    username: [
      {
        required: true,
        message: t('pages.system.accountCenter.account.deletion.validation.usernameRequired'),
        trigger: 'blur'
      },
      {
        validator: (_rule, value, callback) => {
          if ((value || '').trim() !== props.username) {
            callback(new Error(t('pages.system.accountCenter.account.deletion.validation.usernameMismatch')))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }))

  let cooldownTimer: number | undefined

  const clearCooldown = () => {
    if (cooldownTimer) {
      window.clearInterval(cooldownTimer)
      cooldownTimer = undefined
    }
  }

  const startCooldown = () => {
    cooldown.value = 60
    clearCooldown()
    cooldownTimer = window.setInterval(() => {
      cooldown.value -= 1
      if (cooldown.value <= 0) {
        clearCooldown()
      }
    }, 1000)
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        step.value = 'verify'
        form.currentPassword = ''
        form.emailCode = ''
        form.username = ''
        verifyFormRef.value?.clearValidate()
        confirmFormRef.value?.clearValidate()
        return
      }
      clearCooldown()
      cooldown.value = 0
    }
  )

  const sendCode = async () => {
    sendingCode.value = true
    try {
      await fetchSendMfaEmailCode()
      ElMessage.success(t('pages.system.accountCenter.account.deletion.codeSent'))
      startCooldown()
    } finally {
      sendingCode.value = false
    }
  }

  const nextStep = async () => {
    if (mfaBlocking.value) {
      ElMessage.warning(t('pages.system.accountCenter.account.deletion.mfaBlocking'))
      return
    }
    const valid = await verifyFormRef.value?.validate().catch(() => false)
    if (!valid) return
    step.value = 'confirm'
    confirmFormRef.value?.clearValidate()
  }

  const submit = async () => {
    if (mfaBlocking.value) {
      ElMessage.warning(t('pages.system.accountCenter.account.deletion.mfaBlocking'))
      return
    }
    const valid = await confirmFormRef.value?.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await fetchRequestAccountDeletion({
        username: form.username.trim(),
        emailCode: hasSecurityEmail.value ? form.emailCode.trim() : undefined,
        currentPassword: hasSecurityEmail.value ? undefined : form.currentPassword
      })
      ElMessage.success(t('pages.system.accountCenter.account.deletion.success'))
      emit('success')
      dialogVisible.value = false
      await accountStore.logOut({ remote: false })
    } finally {
      submitting.value = false
    }
  }
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    :title="t('pages.system.accountCenter.account.deletion.dialogTitle')"
    width="560px"
    align-center
    :close-on-click-modal="false"
    :lock-scroll="false"
  >
    <div class="mb-4 text-xs text-g-500">
      {{
        step === 'verify'
          ? t('pages.system.accountCenter.account.deletion.steps.verify')
          : t('pages.system.accountCenter.account.deletion.steps.confirm')
      }}
    </div>

    <ElAlert
      v-if="mfaBlocking"
      type="warning"
      :closable="false"
      :title="t('pages.system.accountCenter.account.deletion.mfaBlocking')"
      class="mb-4"
    />

    <div v-if="step === 'verify'" class="danger-dialog">
      <p class="mb-4 text-sm text-g-600">
        {{
          hasSecurityEmail
            ? t('pages.system.accountCenter.account.deletion.withEmailDesc', { email: securityEmail })
            : t('pages.system.accountCenter.account.deletion.withPasswordDesc')
        }}
      </p>

      <ElForm ref="verifyFormRef" :model="form" :rules="verifyRules" label-position="top">
        <ElFormItem
          v-if="!hasSecurityEmail"
          :label="t('pages.system.accountCenter.account.deletion.currentPassword')"
          prop="currentPassword"
        >
          <ElInput v-model="form.currentPassword" type="password" show-password />
        </ElFormItem>

        <ElFormItem
          v-if="hasSecurityEmail"
          :label="t('pages.system.accountCenter.account.deletion.emailCode')"
          prop="emailCode"
        >
          <div class="flex w-full gap-2">
            <ElInput v-model="form.emailCode" maxlength="6" />
            <ElButton
              class="danger-dialog__send-code"
              :disabled="cooldown > 0 || sendingCode"
              :loading="sendingCode"
              @click="sendCode"
            >
              {{ cooldown > 0 ? `${cooldown}s` : t('pages.system.accountCenter.account.deletion.sendCode') }}
            </ElButton>
          </div>
        </ElFormItem>
      </ElForm>
    </div>

    <div v-else class="danger-dialog">
      <ElAlert
        type="error"
        :closable="false"
        :title="t('pages.system.accountCenter.account.deletion.alertTitle')"
        class="mb-4"
      />

      <p class="mb-4 text-sm text-g-600">
        {{ t('pages.system.accountCenter.account.deletion.impactHint') }}
      </p>

      <ElForm ref="confirmFormRef" :model="form" :rules="confirmRules" label-position="top">
        <ElFormItem
          :label="
            t('pages.system.accountCenter.account.deletion.usernameConfirm', {
              username
            })
          "
          prop="username"
        >
          <ElInput v-model="form.username" />
        </ElFormItem>
      </ElForm>
    </div>

    <template #footer>
      <ElButton v-if="step === 'confirm'" @click="step = 'verify'">{{ t('common.previous') }}</ElButton>
      <ElButton v-else @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton
        v-if="step === 'verify'"
        type="primary"
        :disabled="mfaBlocking"
        @click="nextStep"
      >
        {{ t('common.next') }}
      </ElButton>
      <ElButton v-else type="danger" :loading="submitting" :disabled="mfaBlocking" @click="submit">
        {{ t('pages.system.accountCenter.account.deletion.action') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
  .danger-dialog {
    min-height: 168px;
  }

  .danger-dialog__send-code {
    width: 116px;
    flex: none;
  }
</style>
