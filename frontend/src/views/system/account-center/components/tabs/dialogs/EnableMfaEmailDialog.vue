<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchSendMfaEmailCode, fetchUpdateMfaEmail } from '@/api/security'

  const props = defineProps<{
    visible: boolean
    enable: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const form = reactive({ code: '' })
  const cooldown = ref(0)
  const sending = ref(false)
  const submitting = ref(false)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const dialogTitle = computed(() =>
    props.enable
      ? t('pages.system.accountCenter.security.mfa.email.enableTitle')
      : t('pages.system.accountCenter.security.mfa.email.disableTitle')
  )

  const dialogTip = computed(() =>
    props.enable
      ? t('pages.system.accountCenter.security.mfa.email.enableTip')
      : t('pages.system.accountCenter.security.mfa.email.disableTip')
  )

  const rules: FormRules = {
    code: [
      {
        required: true,
        message: t('pages.system.accountCenter.security.mfa.email.codeRequired'),
        trigger: 'blur'
      }
    ]
  }

  watch(
    () => props.visible,
    (v) => {
      if (!v) {
        form.code = ''
        cooldown.value = 0
        formRef.value?.clearValidate()
      }
    }
  )

  let cooldownTimer: number | undefined
  const startCooldown = () => {
    cooldown.value = 60
    cooldownTimer = window.setInterval(() => {
      cooldown.value -= 1
      if (cooldown.value <= 0) {
        window.clearInterval(cooldownTimer)
        cooldownTimer = undefined
      }
    }, 1000)
  }

  const sendCode = async () => {
    sending.value = true
    try {
      await fetchSendMfaEmailCode()
      ElMessage.success(t('pages.system.accountCenter.security.mfa.email.codeSent'))
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
      await fetchUpdateMfaEmail({ enabled: props.enable, code: form.code.trim() })
      ElMessage.success(
        props.enable
          ? t('pages.system.accountCenter.security.mfa.email.enableSuccess')
          : t('pages.system.accountCenter.security.mfa.email.disableSuccess')
      )
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
    :title="dialogTitle"
    width="480px"
    align-center
    :close-on-click-modal="false"
    :lock-scroll="false"
  >
    <div class="text-sm text-g-700 mb-4">
      {{ dialogTip }}
    </div>
    <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
      <ElFormItem :label="t('pages.system.accountCenter.security.mfa.email.code')" prop="code">
        <div class="flex gap-2 w-full">
          <ElInput
            v-model="form.code"
            :placeholder="t('pages.system.accountCenter.security.mfa.email.codePlaceholder')"
            maxlength="6"
          />
          <ElButton
            class="mfa-code-button"
            :disabled="cooldown > 0 || sending"
            :loading="sending"
            @click="sendCode"
          >
            {{
              cooldown > 0
                ? `${cooldown}s`
                : t('pages.system.accountCenter.security.mfa.email.sendCode')
            }}
          </ElButton>
        </div>
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
  .mfa-code-button {
    width: 104px;
    flex: 0 0 104px;
  }
</style>
