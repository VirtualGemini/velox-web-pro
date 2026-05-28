<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchSendEmailUnbindCode, fetchUnbindEmail } from '@/api/security'

  const props = defineProps<{
    visible: boolean
    email?: string
    totpEnabled: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const sending = ref(false)
  const submitting = ref(false)
  const cooldown = ref(0)

  const form = reactive({
    currentEmailCode: '',
    totpCode: ''
  })

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const rules = computed<FormRules>(() => ({
    currentEmailCode: [
      {
        required: true,
        message: t('pages.system.accountCenter.security.email.unbindDialog.currentEmailCodeRequired'),
        trigger: 'blur'
      }
    ],
    totpCode: [
      {
        required: props.totpEnabled,
        message: t('pages.system.accountCenter.security.email.unbindDialog.totpCodeRequired'),
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
        form.currentEmailCode = ''
        form.totpCode = ''
        formRef.value?.clearValidate()
        return
      }
      clearCooldown()
      cooldown.value = 0
    }
  )

  const sendCode = async () => {
    sending.value = true
    try {
      await fetchSendEmailUnbindCode()
      ElMessage.success(t('pages.system.accountCenter.security.email.unbindDialog.codeSent'))
      startCooldown()
    } finally {
      sending.value = false
    }
  }

  const submit = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await fetchUnbindEmail({
        currentEmailCode: form.currentEmailCode.trim(),
        totpCode: props.totpEnabled ? form.totpCode.trim() : undefined
      })
      ElMessage.success(t('pages.system.accountCenter.security.email.unbindDialog.success'))
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
    :title="t('pages.system.accountCenter.security.email.unbindDialog.title')"
    width="480px"
    align-center
    :close-on-click-modal="false"
    :lock-scroll="false"
  >
    <div class="mb-4 text-sm text-g-600">
      {{ t('pages.system.accountCenter.security.email.unbindDialog.tip') }}
    </div>

    <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
      <ElFormItem
        :label="
          t('pages.system.accountCenter.security.email.unbindDialog.currentEmailCode', {
            email: email || ''
          })
        "
        prop="currentEmailCode"
      >
        <div class="flex gap-2 w-full">
          <ElInput
            v-model="form.currentEmailCode"
            :placeholder="
              t('pages.system.accountCenter.security.email.unbindDialog.currentEmailCodePlaceholder')
            "
            maxlength="6"
          />
          <ElButton
            class="unbind-email-code-button"
            :disabled="cooldown > 0 || sending"
            :loading="sending"
            @click="sendCode"
          >
            {{
              cooldown > 0
                ? `${cooldown}s`
                : t('pages.system.accountCenter.security.email.unbindDialog.sendCode')
            }}
          </ElButton>
        </div>
      </ElFormItem>

      <ElFormItem
        v-if="totpEnabled"
        :label="t('pages.system.accountCenter.security.email.unbindDialog.totpCode')"
        prop="totpCode"
      >
        <ElInput
          v-model="form.totpCode"
          :placeholder="t('pages.system.accountCenter.security.email.unbindDialog.totpCodePlaceholder')"
          maxlength="6"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton type="primary" :loading="submitting" @click="submit">
        {{ t('common.confirm') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
  .unbind-email-code-button {
    width: 104px;
    flex: 0 0 104px;
  }
</style>
