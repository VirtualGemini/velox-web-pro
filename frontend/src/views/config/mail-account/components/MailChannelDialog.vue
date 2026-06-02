<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchMailChannelList, fetchMailChannelUpdateActive, type MailChannel } from '@/api/mail'

  const props = defineProps<{
    visible: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }>()

  const { t } = useI18n()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const submitting = ref(false)
  const channels = ref<MailChannel[]>([])
  const checkedIds = ref<string[]>([])

  async function loadChannels() {
    loading.value = true
    try {
      const list = await fetchMailChannelList()
      channels.value = list || []
      checkedIds.value = channels.value.filter((item) => item.active === 1).map((item) => item.id)
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        loadChannels()
      }
    }
  )

  async function handleConfirm() {
    submitting.value = true
    try {
      await fetchMailChannelUpdateActive(checkedIds.value)
      ElMessage.success(t('pages.config.mailAccount.channel.saveSuccess'))
      emit('success')
      dialogVisible.value = false
    } finally {
      submitting.value = false
    }
  }
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    :title="t('pages.config.mailAccount.channel.title')"
    width="460px"
    align-center
    :close-on-click-modal="false"
    :lock-scroll="false"
  >
    <div class="mb-4 text-sm text-g-600">
      {{ t('pages.config.mailAccount.channel.tip') }}
    </div>

    <ElEmpty
      v-if="!loading && channels.length === 0"
      :description="t('pages.config.mailAccount.channel.empty')"
    />
    <ElCheckboxGroup v-else v-model="checkedIds" v-loading="loading" class="mail-channel-list">
      <ElCheckbox
        v-for="channel in channels"
        :key="channel.id"
        :value="channel.id"
        class="mail-channel-item"
      >
        <span class="mail-channel-name">{{ channel.name }}</span>
        <ElTag size="small" type="info" class="ml-2">{{ channel.protocol }}</ElTag>
        <span class="mail-channel-count">
          {{ t('pages.config.mailAccount.channel.accountCount', { count: channel.accountCount }) }}
        </span>
      </ElCheckbox>
    </ElCheckboxGroup>

    <template #footer>
      <ElButton @click="dialogVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton type="primary" @click="handleConfirm">{{ t('common.confirm') }}</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped lang="scss">
  .mail-channel-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;

    .mail-channel-item {
      display: flex;
      align-items: center;
      height: auto;
      padding: 10px 12px;
      margin-right: 0;
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;

      :deep(.el-checkbox__label) {
        display: flex;
        flex: 1;
        align-items: center;
        min-width: 0;
      }
    }

    .mail-channel-name {
      font-weight: 500;
    }

    .mail-channel-count {
      margin-left: auto;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
</style>
