<template>
  <ElDialog
    v-model="dialogVisible"
    :title="
      dialogType === 'add'
        ? t('pages.system.account.dialog.addTitle')
        : t('pages.system.account.dialog.editTitle')
    "
    width="520px"
    align-center
    @closed="emit('closed')"
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="92px">
      <ElFormItem :label="t('pages.system.account.dialog.fields.username')" prop="username">
        <ElInput
          v-model="formData.username"
          :placeholder="t('pages.system.account.dialog.placeholders.username')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.account.dialog.fields.password')" prop="password">
        <ElInput
          v-model="formData.password"
          type="password"
          show-password
          autocomplete="new-password"
          :placeholder="
            dialogType === 'add'
              ? t('pages.system.account.dialog.placeholders.initialPassword')
              : t('pages.system.account.dialog.placeholders.passwordOptional')
          "
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.account.dialog.fields.remark')" prop="remark">
        <ElInput
          v-model="formData.remark"
          type="textarea"
          :rows="4"
          :maxlength="255"
          show-word-limit
          :placeholder="t('pages.system.account.dialog.placeholders.remark')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.system.account.dialog.fields.roleCodes')" prop="roleCodes">
        <ElSelect v-model="formData.roleCodes" multiple filterable>
          <ElOption
            v-for="role in roleList"
            :key="role.roleCode"
            :value="role.roleCode"
            :label="role.roleName"
          />
        </ElSelect>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" @click="handleSubmit">{{ t('table.form.submit') }}</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchGetRoleList } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'

  interface Props {
    visible: boolean
    type: string
    accountData?: Partial<Api.SystemManage.AccountListItem> & {
      roleCodes?: string[]
    }
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', value: Api.SystemManage.AccountSaveCommand): void
    (e: 'closed'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { t } = useI18n()

  const roleList = ref<Api.SystemManage.RoleOption[]>([])

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)
  const formRef = ref<FormInstance>()

  const formData = reactive({
    username: '',
    password: '',
    remark: '',
    roleCodes: [] as string[]
  })

  const rules = computed<FormRules>(() => ({
    username: [
      {
        required: true,
        message: t('pages.system.account.dialog.validation.usernameRequired'),
        trigger: 'blur'
      },
      {
        min: 2,
        max: 50,
        message: t('pages.system.account.dialog.validation.usernameLength'),
        trigger: 'blur'
      }
    ],
    password:
      dialogType.value === 'add'
        ? [
            {
              required: true,
              message: t('pages.system.account.dialog.validation.passwordRequired'),
              trigger: 'blur'
            },
            {
              min: 6,
              max: 32,
              message: t('pages.system.account.dialog.validation.passwordLength'),
              trigger: 'blur'
            }
          ]
        : [
            {
              min: 6,
              max: 32,
              message: t('pages.system.account.dialog.validation.passwordLength'),
              trigger: 'blur'
            }
          ],
    remark: [
      {
        max: 255,
        message: t('pages.system.account.dialog.validation.remarkLength'),
        trigger: 'blur'
      }
    ],
    roleCodes: [
      {
        required: true,
        message: t('pages.system.account.dialog.validation.roleRequired'),
        trigger: 'change'
      }
    ]
  }))

  const initFormData = () => {
    const isEdit = props.type === 'edit' && props.accountData
    const row = props.accountData

    Object.assign(formData, {
      username: isEdit && row ? row.username || '' : '',
      password: '',
      remark: isEdit && row ? row.remark || '' : '',
      roleCodes: isEdit && row ? (Array.isArray(row.roleCodes) ? row.roleCodes : []) : []
    })
  }

  const loadRoleList = async () => {
    const response = await fetchGetRoleList({ current: 1, size: 1000 })
    roleList.value = response.records.map((item) => ({
      roleId: item.roleId,
      roleCode: item.roleCode,
      roleName: item.roleName
    }))
  }

  watch(
    () => [props.visible, props.type, props.accountData],
    ([visible]) => {
      if (visible) {
        loadRoleList()
        initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    emit('submit', {
      username: formData.username.trim(),
      password: formData.password.trim() || undefined,
      remark: formData.remark.trim() || undefined,
      roleCodes: [...formData.roleCodes]
    })
  }
</script>
