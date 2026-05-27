<script setup lang="ts">
  import { reactive, ref } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchUpdateUserPassword } from '@/api/auth'

  const { t } = useI18n()
  const isEditPwd = ref(false)
  const pwdFormRef = ref<FormInstance>()

  const pwdForm = reactive({
    password: '',
    newPassword: '',
    confirmPassword: ''
  })

  const pwdRules = reactive<FormRules>({
    password: [
      {
        required: true,
        message: t('pages.system.userCenter.password.validation.currentPasswordRequired'),
        trigger: 'blur'
      }
    ],
    newPassword: [
      {
        required: true,
        message: t('pages.system.userCenter.password.validation.newPasswordRequired'),
        trigger: 'blur'
      },
      {
        min: 6,
        max: 32,
        message: t('pages.system.userCenter.password.validation.passwordLength'),
        trigger: 'blur'
      }
    ],
    confirmPassword: [
      {
        required: true,
        message: t('pages.system.userCenter.password.validation.confirmPasswordRequired'),
        trigger: 'blur'
      },
      {
        validator: (_rule, value, callback) => {
          if (value !== pwdForm.newPassword) {
            callback(new Error(t('pages.system.userCenter.password.validation.passwordMismatch')))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  })

  const resetPwdForm = () => {
    pwdForm.password = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value?.clearValidate()
  }

  const editPwd = async () => {
    if (!isEditPwd.value) {
      isEditPwd.value = true
      resetPwdForm()
      return
    }

    const valid = await pwdFormRef.value?.validate().catch(() => false)
    if (!valid) return

    await fetchUpdateUserPassword({
      currentPassword: pwdForm.password.trim(),
      newPassword: pwdForm.newPassword.trim(),
      confirmPassword: pwdForm.confirmPassword.trim()
    })

    ElMessage.success(t('pages.system.userCenter.messages.passwordUpdateSuccess'))
    isEditPwd.value = false
    resetPwdForm()
  }
</script>

<template>
  <div class="velox-card-sm">
    <h1 class="p-4 text-xl font-normal border-b border-g-300">
      {{ t('pages.system.userCenter.password.title') }}
    </h1>

    <ElForm
      ref="pwdFormRef"
      :model="pwdForm"
      :rules="pwdRules"
      class="box-border p-5"
      label-width="86px"
      label-position="top"
    >
      <ElFormItem
        :label="t('pages.system.userCenter.password.fields.currentPassword')"
        prop="password"
      >
        <ElInput v-model="pwdForm.password" type="password" :disabled="!isEditPwd" show-password />
      </ElFormItem>

      <ElFormItem
        :label="t('pages.system.userCenter.password.fields.newPassword')"
        prop="newPassword"
      >
        <ElInput
          v-model="pwdForm.newPassword"
          type="password"
          :disabled="!isEditPwd"
          show-password
        />
      </ElFormItem>

      <ElFormItem
        :label="t('pages.system.userCenter.password.fields.confirmPassword')"
        prop="confirmPassword"
      >
        <ElInput
          v-model="pwdForm.confirmPassword"
          type="password"
          :disabled="!isEditPwd"
          show-password
        />
      </ElFormItem>

      <div class="flex-c justify-end [&_.el-button]:!w-27.5">
        <ElButton type="primary" class="w-22.5" v-ripple @click="editPwd">
          {{
            isEditPwd
              ? t('pages.system.userCenter.actions.save')
              : t('pages.system.userCenter.actions.edit')
          }}
        </ElButton>
      </div>
    </ElForm>
  </div>
</template>
