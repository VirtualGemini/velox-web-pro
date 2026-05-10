<!-- 个人中心页面 -->
<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <div class="art-card-sm relative p-9 pb-6 overflow-hidden text-center">
          <img class="absolute top-0 left-0 w-full h-50 object-cover" src="@imgs/user/bg.webp" />
          <div class="relative z-10 w-20 h-20 mx-auto mt-30">
            <el-upload
              class="avatar-uploader"
              :http-request="handleAvatarUpload"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
            >
              <img
                class="w-20 h-20 object-cover border-2 border-white rounded-full cursor-pointer hover:opacity-80"
                :src="userAvatar"
              />
              <div
                class="absolute bottom-0 right-0 w-6 h-6 bg-g-500 rounded-full flex items-center justify-center cursor-pointer"
              >
                <ArtSvgIcon icon="ri:upload-line" class="text-white text-xs" />
              </div>
            </el-upload>
          </div>
          <h2 class="mt-5 text-xl font-normal">{{ displayName }}</h2>
          <p class="mt-5 text-sm">{{ profileDescription }}</p>

          <div class="w-75 mx-auto mt-7.5 text-left">
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:mail-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ displayEmail }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:user-3-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ displayPosition }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:map-pin-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ displayAddress }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:dribbble-fill" class="text-g-700" />
              <span class="ml-2 text-sm">{{ displayCompany }}</span>
            </div>
          </div>

          <div class="mt-10">
            <h3 class="text-sm font-medium">标签</h3>
            <div class="flex flex-wrap justify-center mt-3.5">
              <div
                v-for="item in lableList"
                :key="item"
                class="py-1 px-1.5 mr-2.5 mb-2.5 text-xs border border-g-300 rounded"
              >
                {{ item }}
              </div>
              <div v-if="!lableList.length" class="py-1 px-1.5 text-xs border border-g-300 rounded"
                >暂无标签</div
              >
            </div>
          </div>
        </div>
      </div>
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <div class="art-card-sm">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">基本设置</h1>

          <ElForm
            :model="form"
            class="box-border p-5 [&>.el-row_.el-form-item]:w-[calc(50%-10px)] [&>.el-row_.el-input]:w-full [&>.el-row_.el-select]:w-full"
            ref="ruleFormRef"
            :rules="rules"
            label-width="86px"
            label-position="top"
          >
            <ElRow>
              <ElFormItem label="姓名" prop="realName">
                <ElInput v-model="form.realName" :disabled="!isEdit" />
              </ElFormItem>
              <ElFormItem label="性别" prop="sex" class="ml-5">
                <ElSelect v-model="form.sex" placeholder="请选择性别" :disabled="!isEdit">
                  <ElOption
                    v-for="item in options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="昵称" prop="nikeName">
                <ElInput v-model="form.nikeName" :disabled="!isEdit" />
              </ElFormItem>
              <ElFormItem label="邮箱" prop="email" class="ml-5">
                <ElInput v-model="form.email" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="手机" prop="mobile">
                <ElInput v-model="form.mobile" :disabled="!isEdit" />
              </ElFormItem>
              <ElFormItem label="地址" prop="address" class="ml-5">
                <ElInput v-model="form.address" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <ElFormItem label="个人介绍" prop="des" class="h-32">
              <ElInput type="textarea" :rows="4" v-model="form.des" :disabled="!isEdit" />
            </ElFormItem>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton type="primary" class="w-22.5" v-ripple @click="edit">
                {{ isEdit ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>

        <div class="art-card-sm my-5">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">更改密码</h1>

          <ElForm
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            class="box-border p-5"
            label-width="86px"
            label-position="top"
          >
            <ElFormItem label="当前密码" prop="password">
              <ElInput
                v-model="pwdForm.password"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <ElFormItem label="新密码" prop="newPassword">
              <ElInput
                v-model="pwdForm.newPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <ElFormItem label="确认新密码" prop="confirmPassword">
              <ElInput
                v-model="pwdForm.confirmPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton type="primary" class="w-22.5" v-ripple @click="editPwd">
                {{ isEditPwd ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import avatarImg from '@imgs/user/avatar.webp'
  import {
    fetchUpdateUserAvatar,
    fetchUpdateUserPassword,
    fetchUpdateUserProfile
  } from '@/api/auth'
  import { fetchFileUpload } from '@/api/file'
  import { useUserStore } from '@/store/modules/user'
  import type { FormInstance, FormRules, UploadRawFile } from 'element-plus'

  defineOptions({ name: 'UserCenter' })

  const userStore = useUserStore()
  const userInfo = computed(() => userStore.getUserInfo)

  const isEdit = ref(false)
  const isEditPwd = ref(false)
  const ruleFormRef = ref<FormInstance>()
  const pwdFormRef = ref<FormInstance>()

  const form = reactive({
    realName: '',
    nikeName: '',
    email: '',
    mobile: '',
    address: '',
    sex: 0,
    des: ''
  })

  const pwdForm = reactive({
    password: '',
    newPassword: '',
    confirmPassword: ''
  })

  const rules = reactive<FormRules>({
    realName: [
      { required: true, message: '请输入姓名', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    nikeName: [
      { required: true, message: '请输入昵称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
    mobile: [{ required: true, message: '请输入手机号码', trigger: 'blur' }],
    address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
    sex: [{ required: true, message: '请选择性别', trigger: 'change' }]
  })

  const pwdRules = reactive<FormRules>({
    password: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, max: 32, message: '密码长度应在 6 到 32 个字符之间', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
      {
        validator: (_rule, value, callback) => {
          if (value !== pwdForm.newPassword) {
            callback(new Error('两次输入的新密码不一致'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  })

  const options = [
    { value: 0, label: '未知' },
    { value: 1, label: '男' },
    { value: 2, label: '女' },
    { value: 3, label: '其它' }
  ]

  const userAvatar = computed(() => userInfo.value.avatar || avatarImg)
  const displayName = computed(
    () => userInfo.value.nickname || userInfo.value.userName || '未设置昵称'
  )
  const displayEmail = computed(() => userInfo.value.email || '未设置邮箱')
  const displayPosition = computed(() => userInfo.value.position || '未设置岗位')
  const displayAddress = computed(() => userInfo.value.address || '未设置地址')
  const displayCompany = computed(() => userInfo.value.company || '未设置在职企业')
  const profileDescription = computed(
    () =>
      userInfo.value.signature || userInfo.value.introduction || '这个人很神秘，还没有留下个人介绍'
  )
  const lableList = computed(() => userInfo.value.tags || [])

  const syncForm = () => {
    form.realName = userInfo.value.realName || ''
    form.nikeName = userInfo.value.nickname || userInfo.value.userName || ''
    form.email = userInfo.value.email || ''
    form.mobile = userInfo.value.phone || ''
    form.address = userInfo.value.address || ''
    form.sex = userInfo.value.gender ?? 0
    form.des = userInfo.value.introduction || ''
  }

  const resetPwdForm = () => {
    pwdForm.password = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value?.clearValidate()
  }

  watch(
    () => userInfo.value,
    () => {
      if (!isEdit.value) {
        syncForm()
      }
    },
    { immediate: true, deep: true }
  )

  const handleAvatarUpload = async (options: { file: File }) => {
    try {
      const avatarUrl = await fetchFileUpload(options.file, 'avatar')
      const nextUserInfo = await fetchUpdateUserAvatar({ avatarUrl })
      userStore.setUserInfo(nextUserInfo)
      ElMessage.success('头像上传成功')
    } catch {
      ElMessage.error('头像上传失败')
    }
  }

  const beforeAvatarUpload = (file: UploadRawFile) => {
    const isImage = file.type?.startsWith('image/')
    const isLt2M = (file.size || 0) / 1024 / 1024 < 2

    if (!isImage) {
      ElMessage.error('只能上传图片文件!')
      return false
    }
    if (!isLt2M) {
      ElMessage.error('图片大小不能超过 2MB!')
      return false
    }
    return true
  }

  const edit = async () => {
    if (!isEdit.value) {
      isEdit.value = true
      return
    }

    const valid = await ruleFormRef.value?.validate().catch(() => false)
    if (!valid) return

    const payload: Api.Auth.UserProfileUpdateCommand = {
      realName: form.realName.trim(),
      nickname: form.nikeName.trim(),
      email: form.email.trim() || undefined,
      phone: form.mobile.trim(),
      address: form.address.trim() || undefined,
      gender: form.sex,
      introduction: form.des.trim() || undefined
    }

    await fetchUpdateUserProfile(payload)

    userStore.setUserInfo({
      buttons: userInfo.value.buttons || [],
      roles: userInfo.value.roles || [],
      tags: userInfo.value.tags || [],
      userId: userInfo.value.userId || '',
      userName: userInfo.value.userName || '',
      email: payload.email || '',
      phone: payload.phone,
      avatar: userInfo.value.avatar,
      nickname: payload.nickname,
      gender: payload.gender,
      realName: payload.realName,
      address: payload.address || '',
      introduction: payload.introduction || '',
      signature: userInfo.value.signature || '',
      position: userInfo.value.position || '',
      company: userInfo.value.company || ''
    })

    ElMessage.success('资料保存成功')
    isEdit.value = false
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

    ElMessage.success('密码修改成功')
    isEditPwd.value = false
    resetPwdForm()
  }
</script>
