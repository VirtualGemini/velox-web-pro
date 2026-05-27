<script setup lang="ts">
  import avatarImg from '@imgs/user/avatar.webp'
  import { computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import type { UploadRawFile } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { fetchUpdateUserAvatar } from '@/api/auth'
  import { fetchFileUpload } from '@/api/file'
  import { useUserStore } from '@/store/modules/user'

  const props = defineProps<{
    detail: Partial<Api.Auth.UserDetail>
  }>()

  const emit = defineEmits<{
    (e: 'avatar-updated', avatar: string): void
  }>()

  const { t } = useI18n()
  const userStore = useUserStore()

  const userAvatar = computed(() => props.detail.avatar || avatarImg)
  const displayName = computed(
    () =>
      props.detail.nickname ||
      props.detail.userName ||
      t('pages.system.userCenter.profile.defaults.nickname')
  )
  const displayEmail = computed(
    () => props.detail.email || t('pages.system.userCenter.profile.defaults.email')
  )
  const displayPosition = computed(
    () => props.detail.position || t('pages.system.userCenter.profile.defaults.position')
  )
  const displayAddress = computed(
    () => props.detail.address || t('pages.system.userCenter.profile.defaults.address')
  )
  const displayCompany = computed(
    () => props.detail.company || t('pages.system.userCenter.profile.defaults.company')
  )
  const profileDescription = computed(
    () =>
      props.detail.signature ||
      props.detail.introduction ||
      t('pages.system.userCenter.profile.defaults.description')
  )
  const labelList = computed(() => props.detail.tags || [])

  const beforeAvatarUpload = (file: UploadRawFile) => {
    const isImage = file.type?.startsWith('image/')
    const isLt2M = (file.size || 0) / 1024 / 1024 < 2
    if (!isImage) {
      ElMessage.error(t('pages.system.userCenter.messages.onlyImageAllowed'))
      return false
    }
    if (!isLt2M) {
      ElMessage.error(t('pages.system.userCenter.messages.imageSizeLimit'))
      return false
    }
    return true
  }

  const handleAvatarUpload = async (options: { file: File }) => {
    try {
      const avatarUrl = await fetchFileUpload(options.file, 'avatar')
      const nextUserInfo = await fetchUpdateUserAvatar({ avatarUrl })
      userStore.setUserInfo(nextUserInfo)
      emit('avatar-updated', nextUserInfo.avatar || '')
      ElMessage.success(t('pages.system.userCenter.messages.avatarUploadSuccess'))
    } catch {
      ElMessage.error(t('pages.system.userCenter.messages.avatarUploadFailed'))
    }
  }
</script>

<template>
  <div class="velox-card-sm relative p-9 pb-6 overflow-hidden text-center">
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
          <VeloxSvgIcon icon="ri:upload-line" class="text-white text-xs" />
        </div>
      </el-upload>
    </div>
    <h2 class="mt-5 text-xl font-normal">{{ displayName }}</h2>
    <p class="mt-5 text-sm">{{ profileDescription }}</p>

    <div class="w-75 mx-auto mt-7.5 text-left">
      <div class="mt-2.5">
        <VeloxSvgIcon icon="ri:mail-line" class="text-g-700" />
        <span class="ml-2 text-sm">{{ displayEmail }}</span>
      </div>
      <div class="mt-2.5">
        <VeloxSvgIcon icon="ri:user-3-line" class="text-g-700" />
        <span class="ml-2 text-sm">{{ displayPosition }}</span>
      </div>
      <div class="mt-2.5">
        <VeloxSvgIcon icon="ri:map-pin-line" class="text-g-700" />
        <span class="ml-2 text-sm">{{ displayAddress }}</span>
      </div>
      <div class="mt-2.5">
        <VeloxSvgIcon icon="ri:dribbble-fill" class="text-g-700" />
        <span class="ml-2 text-sm">{{ displayCompany }}</span>
      </div>
    </div>

    <div class="mt-10">
      <h3 class="text-sm font-medium">{{ t('pages.system.userCenter.profile.tags') }}</h3>
      <div class="flex flex-wrap justify-center mt-3.5">
        <div
          v-for="item in labelList"
          :key="item"
          class="py-1 px-1.5 mr-2.5 mb-2.5 text-xs border border-g-300 rounded"
        >
          {{ item }}
        </div>
        <div v-if="!labelList.length" class="py-1 px-1.5 text-xs border border-g-300 rounded">{{
          t('pages.system.userCenter.profile.noTags')
        }}</div>
      </div>
    </div>
  </div>
</template>
