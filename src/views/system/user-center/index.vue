<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { onMounted } from 'vue'
  import { useUserDetail } from './composables/useUserDetail'
  import ProfileCard from './components/ProfileCard.vue'
  import BasicInfoTab from './components/tabs/BasicInfoTab.vue'
  import PasswordTab from './components/tabs/PasswordTab.vue'
  import AccountSecurityTab from './components/tabs/AccountSecurityTab.vue'
  import ThirdPartyTab from './components/tabs/ThirdPartyTab.vue'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'UserCenter' })

  const { t } = useI18n()
  const route = useRoute()
  const router = useRouter()
  const { detail, load, patch, syncStoreBasic } = useUserDetail()

  const validTabs = ['basic', 'password', 'security', 'third-party'] as const
  type TabKey = (typeof validTabs)[number]

  const resolveTab = (raw: unknown): TabKey => {
    const v = String(raw || '')
    return (validTabs as readonly string[]).includes(v) ? (v as TabKey) : 'basic'
  }

  const activeTab = ref<TabKey>(resolveTab(route.query.tab))

  watch(
    () => route.query.tab,
    (v) => {
      activeTab.value = resolveTab(v)
    }
  )

  const onTabChange = (val: string | number) => {
    const next = resolveTab(val)
    activeTab.value = next
    router.replace({ query: { ...route.query, tab: next } })
  }

  onMounted(load)

  const onBasicSaved = (patchValue: Partial<Api.Auth.UserDetail>) => {
    patch(patchValue)
    syncStoreBasic({ email: patchValue.email, phone: patchValue.phone })
  }

  const onDetailUpdated = (patchValue: Partial<Api.Auth.UserDetail>) => {
    patch(patchValue)
    syncStoreBasic({ email: patchValue.email, phone: patchValue.phone })
  }

  const onAvatarUpdated = (avatar: string) => {
    patch({ avatar })
  }

  const cardDetail = computed(() => detail.value)
</script>

<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <ProfileCard :detail="cardDetail" @avatar-updated="onAvatarUpdated" />
      </div>
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <ElTabs :model-value="activeTab" class="velox-user-center-tabs" @tab-change="onTabChange">
          <ElTabPane :label="t('pages.system.userCenter.tabs.basic')" name="basic">
            <BasicInfoTab v-if="activeTab === 'basic'" :detail="cardDetail" @saved="onBasicSaved" />
          </ElTabPane>
          <ElTabPane :label="t('pages.system.userCenter.tabs.password')" name="password">
            <PasswordTab v-if="activeTab === 'password'" />
          </ElTabPane>
          <ElTabPane :label="t('pages.system.userCenter.tabs.security')" name="security">
            <AccountSecurityTab
              v-if="activeTab === 'security'"
              :detail="cardDetail"
              @detail-updated="onDetailUpdated"
            />
          </ElTabPane>
          <ElTabPane :label="t('pages.system.userCenter.tabs.thirdParty')" name="third-party">
            <ThirdPartyTab v-if="activeTab === 'third-party'" />
          </ElTabPane>
        </ElTabs>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .velox-user-center-tabs :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }
</style>
