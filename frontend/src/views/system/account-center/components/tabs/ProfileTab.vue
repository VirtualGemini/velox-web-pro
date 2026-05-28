<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules, UploadRawFile } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import avatarImg from '@imgs/user/avatar.webp'
  import { fetchUpdateAccountAvatar, fetchUpdateAccountProfile } from '@/api/auth'
  import { fetchFileUpload } from '@/api/file'
  import { useAccountStore } from '@/store/modules/user'

  const props = defineProps<{
    detail: Partial<Api.Auth.AccountDetail>
  }>()

  const emit = defineEmits<{
    (e: 'saved', patch: Partial<Api.Auth.AccountDetail>): void
    (e: 'avatar-updated', avatar: string): void
  }>()

  const { t } = useI18n()
  const accountStore = useAccountStore()
  const ruleFormRef = ref<FormInstance>()

  const form = reactive({
    realName: '',
    nickname: '',
    email: '',
    mobile: '',
    address: '',
    sex: 0,
    des: '',
    signature: '',
    position: '',
    company: '',
    tags: [] as string[]
  })

  const syncForm = () => {
    form.realName = props.detail.realName || ''
    form.nickname = props.detail.nickname || props.detail.userName || ''
    form.email = props.detail.email || ''
    form.mobile = props.detail.phone || ''
    form.address = props.detail.address || ''
    form.sex = props.detail.gender ?? 0
    form.des = props.detail.introduction || ''
    form.signature = props.detail.signature || ''
    form.position = props.detail.position || ''
    form.company = props.detail.company || ''
    form.tags = [...(props.detail.tags || [])]
  }

  watch(
    () => props.detail,
    () => {
      syncForm()
    },
    { immediate: true, deep: true }
  )

  const options = computed(() => [
    { value: 0, label: t('pages.system.accountCenter.sex.unknown') },
    { value: 1, label: t('pages.system.accountCenter.sex.male') },
    { value: 2, label: t('pages.system.accountCenter.sex.female') },
    { value: 3, label: t('pages.system.accountCenter.sex.other') }
  ])

  const userAvatar = computed(() => props.detail.avatar || avatarImg)

  const rules = reactive<FormRules>({
    realName: [
      {
        min: 2,
        max: 50,
        message: t('pages.system.accountCenter.basicSettings.validation.nameLength'),
        trigger: 'blur'
      }
    ],
    nickname: [
      {
        min: 2,
        max: 50,
        message: t('pages.system.accountCenter.basicSettings.validation.nameLength'),
        trigger: 'blur'
      }
    ],
    email: [],
    mobile: [],
    address: [],
    sex: [],
    signature: [
      {
        max: 255,
        message: t('pages.system.accountCenter.basicSettings.validation.signatureLength'),
        trigger: 'blur'
      }
    ],
    position: [
      {
        max: 50,
        message: t('pages.system.accountCenter.basicSettings.validation.positionLength'),
        trigger: 'blur'
      }
    ],
    company: [
      {
        max: 100,
        message: t('pages.system.accountCenter.basicSettings.validation.companyLength'),
        trigger: 'blur'
      }
    ]
  })

  const validateTags = (): boolean => {
    if (form.tags.length > 10) {
      ElMessage.error(t('pages.system.accountCenter.basicSettings.validation.tagsLimit'))
      return false
    }
    if (form.tags.some((tag) => (tag || '').length > 16)) {
      ElMessage.error(t('pages.system.accountCenter.basicSettings.validation.tagLength'))
      return false
    }
    return true
  }

  const beforeAvatarUpload = (file: UploadRawFile) => {
    const isImage = file.type?.startsWith('image/')
    const isLt2M = (file.size || 0) / 1024 / 1024 < 2
    if (!isImage) {
      ElMessage.error(t('pages.system.accountCenter.messages.onlyImageAllowed'))
      return false
    }
    if (!isLt2M) {
      ElMessage.error(t('pages.system.accountCenter.messages.imageSizeLimit'))
      return false
    }
    return true
  }

  const handleAvatarUpload = async (options: { file: File }) => {
    try {
      const avatarUrl = await fetchFileUpload(options.file, 'avatar')
      const nextUserInfo = await fetchUpdateAccountAvatar({ avatarUrl })
      accountStore.setAccountInfo(nextUserInfo)
      emit('avatar-updated', nextUserInfo.avatar || '')
      ElMessage.success(t('pages.system.accountCenter.messages.avatarUploadSuccess'))
    } catch {
      ElMessage.error(t('pages.system.accountCenter.messages.avatarUploadFailed'))
    }
  }

  const save = async () => {
    const valid = await ruleFormRef.value?.validate().catch(() => false)
    if (!valid) return
    if (!validateTags()) return

    const payload: Api.Auth.AccountProfileUpdateCommand = {
      realName: form.realName.trim() || undefined,
      nickname: form.nickname.trim() || undefined,
      email: form.email.trim() || undefined,
      phone: form.mobile.trim() || undefined,
      address: form.address.trim() || undefined,
      gender: form.sex || undefined,
      introduction: form.des.trim() || undefined,
      signature: form.signature.trim() || undefined,
      position: form.position.trim() || undefined,
      company: form.company.trim() || undefined,
      tags: form.tags
    }

    await fetchUpdateAccountProfile(payload)

    emit('saved', {
      email: payload.email || '',
      phone: payload.phone || '',
      nickname: payload.nickname || '',
      gender: payload.gender ?? 0,
      realName: payload.realName || '',
      address: payload.address || '',
      introduction: payload.introduction || '',
      signature: payload.signature || '',
      position: payload.position || '',
      company: payload.company || '',
      tags: payload.tags
    })

    ElMessage.success(t('pages.system.accountCenter.messages.profileSaveSuccess'))
  }
</script>

<template>
  <div class="github-profile-settings">
    <header class="border-b border-g-300 pb-3">
      <h1 class="text-xl font-medium leading-8 text-g-900">
        {{ t('pages.system.accountCenter.basicSettings.title') }}
      </h1>
      <p class="mt-1 text-sm leading-6 text-g-600">
        {{ t('pages.system.accountCenter.profile.formDesc') }}
      </p>
    </header>

    <ElForm
      ref="ruleFormRef"
      :model="form"
      class="profile-form"
      :rules="rules"
      label-width="86px"
      label-position="top"
    >
      <section
        class="grid grid-cols-[minmax(0,680px)_220px] gap-10 border-b border-g-300 py-6 max-lg:grid-cols-1"
      >
        <div class="min-w-0 max-lg:order-2">
          <div class="mb-4">
            <h2 class="text-base font-medium leading-6 text-g-900">
              {{ t('pages.system.accountCenter.profile.sections.public') }}
            </h2>
          </div>

          <ElFormItem
            :label="t('pages.system.accountCenter.basicSettings.fields.nickname')"
            prop="nickname"
          >
            <ElInput v-model="form.nickname" />
          </ElFormItem>

          <ElFormItem
            :label="t('pages.system.accountCenter.basicSettings.fields.signature')"
            prop="signature"
          >
            <ElInput
              v-model="form.signature"
              :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.signature')"
            />
          </ElFormItem>

          <ElFormItem
            :label="t('pages.system.accountCenter.basicSettings.fields.description')"
            prop="des"
          >
            <ElInput type="textarea" :rows="5" v-model="form.des" />
          </ElFormItem>

          <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.tags')" prop="tags">
            <ElSelect
              v-model="form.tags"
              multiple
              filterable
              allow-create
              default-first-option
              :max-collapse-tags="3"
              :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.tags')"
              class="w-full"
            />
          </ElFormItem>
        </div>

        <aside class="profile-picture-panel max-lg:order-1 max-lg:max-w-70">
          <h2 class="mb-3 text-base font-medium leading-6 text-g-900">
            {{ t('pages.system.accountCenter.profile.avatar.title') }}
          </h2>
          <ElUpload
            :http-request="handleAvatarUpload"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            accept="image/*"
            class="avatar-upload"
          >
            <div class="avatar-edit-trigger">
              <img class="size-50 rounded-full object-cover ring-1 ring-g-300" :src="userAvatar" />
              <div class="avatar-edit-overlay">
                <VeloxSvgIcon icon="ri:edit-line" class="text-2xl text-white" />
              </div>
            </div>
          </ElUpload>
          <p class="mt-3 text-xs leading-5 text-g-600">
            {{ t('pages.system.accountCenter.profile.avatar.desc') }}
          </p>
        </aside>
      </section>

      <section class="border-b border-g-300 py-6">
        <div class="mb-4">
          <h2 class="text-base font-medium leading-6 text-g-900">
            {{ t('pages.system.accountCenter.profile.sections.private') }}
          </h2>
        </div>

        <div class="grid max-w-170 grid-cols-2 gap-x-5 max-md:grid-cols-1">
          <ElFormItem
            :label="t('pages.system.accountCenter.basicSettings.fields.realName')"
            prop="realName"
          >
            <ElInput v-model="form.realName" />
          </ElFormItem>
          <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.sex')" prop="sex">
            <ElSelect
              v-model="form.sex"
              :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.sex')"
            >
              <ElOption
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>

          <ElFormItem :label="t('pages.system.accountCenter.basicSettings.fields.email')" prop="email">
            <ElInput v-model="form.email" />
          </ElFormItem>
          <ElFormItem
            :label="t('pages.system.accountCenter.basicSettings.fields.mobile')"
            prop="mobile"
          >
            <ElInput v-model="form.mobile" />
          </ElFormItem>

          <ElFormItem
            :label="t('pages.system.accountCenter.basicSettings.fields.position')"
            prop="position"
          >
            <ElInput
              v-model="form.position"
              :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.position')"
            />
          </ElFormItem>
          <ElFormItem
            :label="t('pages.system.accountCenter.basicSettings.fields.company')"
            prop="company"
          >
            <ElInput
              v-model="form.company"
              :placeholder="t('pages.system.accountCenter.basicSettings.placeholders.company')"
            />
          </ElFormItem>

          <ElFormItem
            :label="t('pages.system.accountCenter.basicSettings.fields.address')"
            prop="address"
            class="col-span-2 max-md:col-span-1"
          >
            <ElInput v-model="form.address" />
          </ElFormItem>
        </div>
      </section>

      <div class="pt-4">
        <ElButton type="primary" v-ripple @click="save">
          {{ t('pages.system.accountCenter.actions.save') }}
        </ElButton>
      </div>
    </ElForm>
  </div>
</template>

<style scoped>
  .profile-form :deep(.el-select) {
    width: 100%;
  }

  .profile-form :deep(.el-input),
  .profile-form :deep(.el-textarea) {
    width: 100%;
  }

  .github-profile-settings :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  .github-profile-settings :deep(.el-form-item__label) {
    margin-bottom: 6px;
    color: var(--velox-text-color);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
  }

  .avatar-upload :deep(.el-upload) {
    display: block;
    text-align: left;
  }

  .avatar-edit-trigger {
    position: relative;
    width: 200px;
    height: 200px;
    cursor: pointer;
    border-radius: 9999px;
  }

  .avatar-edit-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgb(0 0 0 / 0%);
    border-radius: 9999px;
    opacity: 0;
    transition:
      background-color 0.18s ease,
      opacity 0.18s ease;
  }

  .avatar-edit-trigger:hover .avatar-edit-overlay {
    background: rgb(0 0 0 / 18%);
    opacity: 1;
  }

  .github-profile-settings {
    padding-bottom: 80px;
  }
</style>
