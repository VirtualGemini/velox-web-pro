<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchRebindEmail, fetchSendEmailRebindCode } from '@/api/security'

  const props = defineProps<{
    visible: boolean
    currentEmail?: string
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success', newEmail: string): void
  }>()

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const form = reactive({ newEmail: '', code: '' })
  const cooldown = ref(0)
  const sending = ref(false)
  const submitting = ref(false)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const rules: FormRules = {
    newEmail: [
      {
        required: true,
        message: t('pages.system.userCenter.security.email.dialog.emailRequired'),
        trigger: 'blur'
      },
      {
        type: 'email',
        message: t('pages.system.userCenter.security.email.dialog.emailInvalid'),
        trigger: 'blur'
      }
    ],
    code: [
      {
        required: true,
        message: t('pages.system.userCenter.security.email.dialog.codeRequired'),
        trigger: 'blur'
      }
    ]
  }

  watch(
    () => props.visible,
    (v) => {
      if (!v) {
        form.newEmail = ''
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
    const ok = await formRef.value?.validateField('newEmail').catch(() => false)
    if (!ok) return
    if (form.newEmail.trim() === (props.currentEmail || '').trim()) {
      ElMessage.warning(t('pages.system.userCenter.security.email.dialog.emailSame'))
      return
    }
    sending.value = true
    try {
      await fetchSendEmailRebindCode({ newEmail: form.newEmail.trim() })
      ElMessage.success(t('pages.system.userCenter.security.email.dialog.codeSent'))
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
      await fetchRebindEmail({ newEmail: form.newEmail.trim(), code: form.code.trim() })
      ElMessage.success(t('pages.system.userCenter.security.email.dialog.success'))
      emit('success', form.newEmail.trim())
      dialogVisible.value = false
    } finally {
      submitting.value = false
    }
  }
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    :title="t('pages.system.userCenter.security.email.dialog.title')"
    width="480px"
    align-center
    :close-on-click-modal="false"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
      <ElFormItem
        :label="t('pages.system.userCenter.security.email.dialog.newEmail')"
        prop="newEmail"
      >
        <ElInput
          v-model="form.newEmail"
          :placeholder="t('pages.system.userCenter.security.email.dialog.newEmailPlaceholder')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.userCenter.security.email.dialog.code')" prop="code">
        <div class="flex gap-2 w-full">
          <ElInput
            v-model="form.code"
            :placeholder="t('pages.system.userCenter.security.email.dialog.codePlaceholder')"
            maxlength="6"
          />
          <ElButton :disabled="cooldown > 0 || sending" :loading="sending" @click="sendCode">
            {{
              cooldown > 0
                ? `${cooldown}s`
                : t('pages.system.userCenter.security.email.dialog.sendCode')
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
