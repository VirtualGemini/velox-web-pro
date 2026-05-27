<script setup lang="ts">
  import { computed } from 'vue'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import linuxdoIcon from '@/assets/images/svg/linuxdo.svg'

  const { t } = useI18n()

  const providers = computed(() => [
    {
      key: 'github',
      icon: 'ri:github-fill',
      name: t('pages.system.userCenter.thirdParty.providers.github')
    },
    {
      key: 'linuxdo',
      iconUrl: linuxdoIcon,
      name: t('pages.system.userCenter.thirdParty.providers.linuxdo')
    }
  ])

  const onBind = () => {
    ElMessage.info(t('pages.system.userCenter.thirdParty.comingSoon'))
  }
</script>

<template>
  <div class="velox-card-sm">
    <h1 class="p-4 text-xl font-normal border-b border-g-300">
      {{ t('pages.system.userCenter.thirdParty.title') }}
    </h1>
    <div class="p-5 space-y-4">
      <div class="text-xs text-g-600">
        {{ t('pages.system.userCenter.thirdParty.desc') }}
      </div>
      <div
        v-for="provider in providers"
        :key="provider.key"
        class="flex items-center justify-between border border-g-300 rounded p-4"
      >
        <div class="flex items-center gap-3">
          <img
            v-if="provider.iconUrl"
            :src="provider.iconUrl"
            :alt="provider.name"
            class="h-6 w-6 object-contain"
          />
          <VeloxSvgIcon v-else :icon="provider.icon" class="text-2xl text-g-700" />
          <div>
            <div class="text-sm font-medium">{{ provider.name }}</div>
            <div class="text-xs text-g-500 mt-1">
              {{ t('pages.system.userCenter.thirdParty.comingSoon') }}
            </div>
          </div>
        </div>
        <ElButton type="primary" link @click="onBind">
          {{ t('pages.system.userCenter.thirdParty.actions.bind') }}
        </ElButton>
      </div>
    </div>
  </div>
</template>
