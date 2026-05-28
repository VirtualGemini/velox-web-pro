<!-- 已登录账号卡片 -->
<template>
  <div
    class="velox-logged-in-card group flex items-center px-3 py-2.5 c-p"
    :class="selected ? 'is-selected' : 'is-default'"
    @click="onSelect"
  >
    <img class="size-9 mr-2.5 rounded-full object-cover shrink-0" :src="avatar" alt="avatar" />

    <div class="flex-1 min-w-0">
      <p class="m-0 text-sm font-medium text-g-800 truncate leading-tight">{{ displayName }}</p>
      <p class="mt-0.5 text-xs text-g-500 truncate leading-tight">{{ subLine }}</p>
    </div>

    <div
      v-if="logoutVisible"
      class="ml-2 shrink-0 size-7 inline-flex flex-cc text-base text-g-600 dark:text-g-800 rounded-full hover:bg-hover-color hover:text-theme"
      :title="logoutTooltip || $t('login.loggedIn.logoutTooltip')"
      @click.stop="onLogout"
    >
      <VeloxSvgIcon icon="ri:logout-box-r-line" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import avatarImg from '@imgs/user/avatar.webp'

  defineOptions({ name: 'VeloxLoggedInCard' })

  interface Props {
    userInfo: Partial<Api.Auth.AccountInfo>
    selected?: boolean
    logoutTooltip?: string
    logoutVisible?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    selected: false,
    logoutVisible: true
  })
  const emit = defineEmits<{
    select: []
    logout: []
  }>()

  const avatar = computed(() => props.userInfo.avatar || avatarImg)

  const displayName = computed(() => props.userInfo.userName || props.userInfo.accountId || '')

  const subLine = computed(() => props.userInfo.email || props.userInfo.userName || '')
  const logoutVisible = computed(() => props.logoutVisible)

  const onSelect = () => emit('select')
  const onLogout = () => emit('logout')
</script>

<style scoped>
  .velox-logged-in-card {
    background-color: transparent;
    border: 1px solid var(--velox-card-border);
    border-radius: calc(var(--custom-radius) + 4px);

    /* 第 1 步：最简过渡 —— 只过渡 background-color，linear，150ms。
       如果这一步就闪，说明 transparent → 半透目标色的 RGB 插值是元凶。 */
    transition: background-color 150ms linear;
  }

  .velox-logged-in-card.is-default:hover {
    background-color: color-mix(in srgb, var(--velox-hover-color) 50%, transparent);
  }

  .velox-logged-in-card.is-selected {
    background-color: color-mix(in srgb, var(--velox-hover-color) 80%, transparent);
  }
</style>
