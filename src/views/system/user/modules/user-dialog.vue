<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
    width="30%"
    align-center
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="80px">
      <ElFormItem label="用户名" prop="username">
        <ElInput v-model="formData.username" placeholder="请输入用户名" />
      </ElFormItem>
      <ElFormItem label="昵称" prop="nickname">
        <ElInput v-model="formData.nickname" placeholder="请输入昵称" />
      </ElFormItem>
      <ElFormItem label="邮箱" prop="email">
        <ElInput v-model="formData.email" placeholder="请输入邮箱" />
      </ElFormItem>
      <ElFormItem label="手机号" prop="phone">
        <ElInput v-model="formData.phone" placeholder="请输入手机号" />
      </ElFormItem>
      <ElFormItem v-if="dialogType === 'add'" label="密码" prop="password">
        <ElInput
          v-model="formData.password"
          type="password"
          show-password
          placeholder="请输入初始密码"
        />
      </ElFormItem>
      <ElFormItem v-else label="密码">
        <ElInput
          v-model="formData.password"
          type="password"
          show-password
          placeholder="留空表示不修改密码"
        />
      </ElFormItem>
      <ElFormItem label="性别" prop="gender">
        <ElSelect v-model="formData.gender">
          <ElOption label="男" :value="1" />
          <ElOption label="女" :value="2" />
          <ElOption label="其它" :value="3" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="角色" prop="roleCodes">
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
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchGetRoleList } from '@/api/system-manage'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    type: string
    userData?: Partial<Api.SystemManage.UserListItem>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', value: Api.SystemManage.UserSaveCommand): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 角色列表数据
  const roleList = ref<Api.SystemManage.RoleOption[]>([])

  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()

  // 表单数据
  const formData = reactive({
    username: '',
    nickname: '',
    email: '',
    phone: '',
    password: '',
    gender: 1,
    roleCodes: [] as string[]
  })

  // 表单验证规则
  const rules: FormRules = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
    ],
    password: [
      {
        validator: (_rule, value, callback) => {
          if (dialogType.value === 'add' && !value) {
            callback(new Error('请输入密码'))
            return
          }
          if (value && (value.length < 6 || value.length > 32)) {
            callback(new Error('密码长度应在 6 到 32 个字符之间'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ],
    gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
    roleCodes: [{ required: true, message: '请选择角色', trigger: 'change' }]
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = () => {
    const isEdit = props.type === 'edit' && props.userData
    const row = props.userData

    Object.assign(formData, {
      username: isEdit && row ? row.userName || '' : '',
      nickname: isEdit && row ? row.nickName || row.userName || '' : '',
      email: isEdit && row ? row.userEmail || '' : '',
      phone: isEdit && row ? row.userPhone || '' : '',
      password: '',
      gender: isEdit && row ? (row.userGender === '女' ? 2 : 1) : 1,
      roleCodes: isEdit && row ? (Array.isArray(row.userRoles) ? row.userRoles : []) : []
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

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.userData],
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

  /**
   * 提交表单
   * 验证通过后触发提交事件
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    emit('submit', {
      username: formData.username.trim(),
      nickname: formData.nickname.trim(),
      email: formData.email.trim() || undefined,
      phone: formData.phone.trim(),
      password: formData.password.trim() || undefined,
      gender: formData.gender,
      roleCodes: [...formData.roleCodes]
    })
  }
</script>
