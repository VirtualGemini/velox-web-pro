<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchUpdateAccountUsername } from '@/api/auth'

  const props = defineProps<{
    visible: boolean
    username: string
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success', username: string): void
  }>()

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const form = reactive({
    username: ''
  })

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        form.username = props.username || ''
        formRef.value?.clearValidate()
      }
    }
  )

  const rules = computed<FormRules>(() => ({
    username: [
      {
        required: true,
        message: t('pages.system.accountCenter.account.username.validation.required'),
        trigger: 'blur'
      },
      {
        min: 3,
        max: 50,
        message: t('pages.system.accountCenter.account.username.validation.length'),
        trigger: 'blur'
      }
    ]
  }))

  const submit = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await fetchUpdateAccountUsername({ username: form.username.trim() })
      ElMessage.success(t('pages.system.accountCenter.account.username.success'))
      emit('success', form.username.trim())
      dialogVisible.value = false
    } finally {
      submitting.value = false
    }
  }
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    :title="t('pages.system.accountCenter.account.username.dialogTitle')"
    width="520px"
    align-center
    :close-on-click-modal="false"
    :lock-scroll="false"
  >
    <div class="rename-dialog">
      <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
        <ElFormItem :label="t('pages.system.accountCenter.account.username.label')" prop="username">
          <ElInput v-model="form.username" :maxlength="50" show-word-limit class="rename-dialog__input" />
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
  .rename-dialog__input {
    width: 320px;
    max-width: 100%;
  }
</style>
