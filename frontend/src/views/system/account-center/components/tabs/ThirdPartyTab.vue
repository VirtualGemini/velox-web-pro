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
      name: t('pages.system.accountCenter.thirdParty.providers.github')
    },
    {
      key: 'linuxdo',
      iconUrl: linuxdoIcon,
      name: t('pages.system.accountCenter.thirdParty.providers.linuxdo')
    }
  ])

  const onBind = () => {
    ElMessage.info(t('pages.system.accountCenter.thirdParty.comingSoon'))
  }
</script>

<template>
  <div class="github-third-party-settings">
    <header class="border-b border-g-300 pb-3">
      <h1 class="text-xl font-medium leading-8 text-g-900">
        {{ t('pages.system.accountCenter.thirdParty.title') }}
      </h1>
      <p class="mt-1 text-sm leading-6 text-g-600">
        {{ t('pages.system.accountCenter.thirdParty.desc') }}
      </p>
    </header>

    <div class="provider-list">
      <div v-for="provider in providers" :key="provider.key" class="provider-row">
        <div class="flex items-center gap-3">
          <img
            v-if="provider.iconUrl"
            :src="provider.iconUrl"
            :alt="provider.name"
            class="h-6 w-6 object-contain"
          />
          <VeloxSvgIcon v-else :icon="provider.icon" class="text-2xl text-g-700" />
          <div class="min-w-0">
            <div class="text-sm font-medium text-g-900">{{ provider.name }}</div>
            <div class="mt-1 text-xs text-g-500">
              {{ t('pages.system.accountCenter.thirdParty.comingSoon') }}
            </div>
          </div>
        </div>
        <ElButton @click="onBind">
          {{ t('pages.system.accountCenter.thirdParty.actions.bind') }}
        </ElButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .provider-list {
    width: 100%;
  }

  .provider-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
    padding: 18px 0;
    border-bottom: 1px solid var(--velox-gray-300);
  }

  @media (max-width: 640px) {
    .provider-row {
      align-items: flex-start;
      flex-direction: column;
      gap: 12px;
    }
  }

  .github-third-party-settings {
    padding-bottom: 80px;
  }
</style>
