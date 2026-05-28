<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchDisableMfaTotp } from '@/api/security'

  const props = defineProps<{ visible: boolean }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const form = reactive({
    verifyType: 'totp' as 'totp' | 'recovery',
    code: '',
    recoveryCode: ''
  })
  const submitting = ref(false)

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const rules = computed<FormRules>(() => ({
    code: [
      {
        required: form.verifyType === 'totp',
        message: t('pages.system.accountCenter.security.mfa.totp.codeRequired'),
        trigger: 'blur'
      },
      {
        pattern: /^\d{6,8}$/,
        message: t('pages.system.accountCenter.security.mfa.totp.codeFormat', { digits: 6 }),
        trigger: 'blur'
      }
    ],
    recoveryCode: [
      {
        required: form.verifyType === 'recovery',
        message: t('pages.system.accountCenter.security.mfa.totp.recoveryCodeRequired'),
        trigger: 'blur'
      },
      {
        pattern: /^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$/,
        message: t('pages.system.accountCenter.security.mfa.totp.recoveryCodeFormat'),
        trigger: 'blur'
      }
    ]
  }))

  watch(
    () => props.visible,
    (v) => {
      if (!v) {
        form.verifyType = 'totp'
        form.code = ''
        form.recoveryCode = ''
        formRef.value?.clearValidate()
      }
    }
  )

  const submit = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await fetchDisableMfaTotp({
        code: form.verifyType === 'totp' ? form.code.trim() : undefined,
        recoveryCode:
          form.verifyType === 'recovery' ? form.recoveryCode.trim().toUpperCase() : undefined
      })
      ElMessage.success(t('pages.system.accountCenter.security.mfa.totp.disableSuccess'))
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
    :title="t('pages.system.accountCenter.security.mfa.totp.disableTitle')"
    width="440px"
    align-center
    :close-on-click-modal="false"
    :lock-scroll="false"
  >
    <div class="text-sm text-g-700 mb-4">
      {{ t('pages.system.accountCenter.security.mfa.totp.disableTip') }}
    </div>
    <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
      <ElFormItem :label="t('pages.system.accountCenter.security.mfa.totp.disableMethod')">
        <ElRadioGroup
          v-model="form.verifyType"
          @change="
            () => {
              form.code = ''
              form.recoveryCode = ''
              formRef?.clearValidate()
            }
          "
        >
          <ElRadioButton label="totp">
            {{ t('pages.system.accountCenter.security.mfa.totp.code') }}
          </ElRadioButton>
          <ElRadioButton label="recovery">
            {{ t('pages.system.accountCenter.security.mfa.totp.recoveryCode') }}
          </ElRadioButton>
        </ElRadioGroup>
        <div class="mt-2 text-xs text-g-500">
          {{
            form.verifyType === 'recovery'
              ? t('pages.system.accountCenter.security.mfa.totp.recoveryCodeHint')
              : t('pages.system.accountCenter.security.mfa.totp.codeHint')
          }}
        </div>
      </ElFormItem>
      <ElFormItem
        v-if="form.verifyType === 'totp'"
        :label="t('pages.system.accountCenter.security.mfa.totp.code')"
        prop="code"
      >
        <ElInput
          v-model="form.code"
          :placeholder="
            t('pages.system.accountCenter.security.mfa.totp.codePlaceholder', { digits: 6 })
          "
          maxlength="8"
        />
      </ElFormItem>
      <ElFormItem
        v-else
        :label="t('pages.system.accountCenter.security.mfa.totp.recoveryCode')"
        prop="recoveryCode"
      >
        <ElInput
          v-model="form.recoveryCode"
          :placeholder="t('pages.system.accountCenter.security.mfa.totp.recoveryCodePlaceholder')"
          maxlength="9"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton type="danger" :loading="submitting" @click="submit">
        {{ t('common.confirm') }}
      </ElButton>
    </template>
  </ElDialog>
</template>
