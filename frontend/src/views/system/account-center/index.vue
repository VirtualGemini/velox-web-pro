<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { onMounted } from 'vue'
  import { useAccountDetail } from './composables/useUserDetail'
  import { useAccountTab } from './composables/useAccountTab'
  import ProfileTab from './components/tabs/ProfileTab.vue'
  import AccountTab from './components/tabs/AccountTab.vue'
  import AccountSecurityTab from './components/tabs/AccountSecurityTab.vue'
  import ThirdPartyTab from './components/tabs/ThirdPartyTab.vue'
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'AccountCenter' })

  const { t } = useI18n()
  const route = useRoute()
  const router = useRouter()
  const { detail, load, patch, syncStoreBasic } = useAccountDetail()
  const { detail: accountTabDetail, load: loadAccountTab, patch: patchAccountTab } = useAccountTab()

  const validTabs = ['profile', 'account', 'security', 'third-party'] as const
  type TabKey = (typeof validTabs)[number]

  const resolveTab = (raw: unknown): TabKey => {
    const v = String(raw || '')
    if (v === 'basic') return 'profile'
    if (v === 'password') return 'security'
    return (validTabs as readonly string[]).includes(v) ? (v as TabKey) : 'profile'
  }

  const activeTab = ref<TabKey>(resolveTab(route.query.tab))

  watch(
    () => route.query.tab,
    (v) => {
      if (route.name !== 'AccountCenter') return
      activeTab.value = resolveTab(v)
    }
  )

  const onTabChange = (val: string | number) => {
    const next = resolveTab(val)
    activeTab.value = next
    router.replace({ query: { ...route.query, tab: next } })
  }

  onMounted(() => {
    load()
    loadAccountTab()
  })

  const onProfileSaved = (patchValue: Partial<Api.Auth.AccountDetail>) => {
    patch(patchValue)
    syncStoreBasic({ email: patchValue.email, phone: patchValue.phone })
  }

  const onDetailUpdated = (patchValue: Partial<Api.Auth.AccountDetail>) => {
    patch(patchValue)
    syncStoreBasic({ email: patchValue.email, phone: patchValue.phone })
  }

  const onAvatarUpdated = (avatar: string) => {
    patch({ avatar })
  }

  const onUsernameUpdated = (username: string) => {
    patchAccountTab({ username })
    patch({ userName: username })
    syncStoreBasic({ userName: username })
  }

  const onDeletionRequested = () => {
    patchAccountTab({ pendingDeletion: true })
  }

  const onAccountTabPatched = (patchValue: Partial<Api.Auth.AccountTabInfo>) => {
    patchAccountTab(patchValue)
  }

  const cardDetail = computed(() => detail.value)
  const accountCardDetail = computed(() => accountTabDetail.value)
</script>

<template>
  <div class="velox-account-center-page velox-full-height">
    <ElCard class="velox-table-card velox-account-center-card">
      <ElTabs :model-value="activeTab" class="velox-account-center-tabs" @tab-change="onTabChange">
        <ElTabPane :label="t('pages.system.accountCenter.tabs.profile')" name="profile">
          <ProfileTab
            v-if="activeTab === 'profile'"
            :detail="cardDetail"
            @saved="onProfileSaved"
            @avatar-updated="onAvatarUpdated"
          />
        </ElTabPane>
        <ElTabPane :label="t('pages.system.accountCenter.tabs.account')" name="account">
          <AccountTab
            v-if="activeTab === 'account'"
            :detail="accountCardDetail"
            @username-updated="onUsernameUpdated"
            @deletion-requested="onDeletionRequested"
          />
        </ElTabPane>
        <ElTabPane :label="t('pages.system.accountCenter.tabs.security')" name="security">
          <AccountSecurityTab
            v-if="activeTab === 'security'"
            :detail="cardDetail"
            @detail-updated="onDetailUpdated"
            @account-tab-updated="onAccountTabPatched"
          />
        </ElTabPane>
        <ElTabPane :label="t('pages.system.accountCenter.tabs.thirdParty')" name="third-party">
          <ThirdPartyTab v-if="activeTab === 'third-party'" />
        </ElTabPane>
      </ElTabs>
    </ElCard>
  </div>
</template>

<style scoped>
.velox-account-center-page {
  width: 100%;
}

.velox-account-center-card :deep(.el-card__body) {
  overflow-y: auto;
}

.velox-account-center-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

  .velox-account-center-tabs :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
</style>
