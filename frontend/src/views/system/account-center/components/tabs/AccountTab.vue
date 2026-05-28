<script setup lang="ts">
  import { reactive, ref, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { fetchUpdateAccountUsername } from '@/api/auth'
  import DeleteAccountDialog from './dialogs/DeleteAccountDialog.vue'

  const props = defineProps<{
    detail: Partial<Api.Auth.AccountTabInfo>
  }>()

  const emit = defineEmits<{
    (e: 'username-updated', username: string): void
    (e: 'deletion-requested'): void
  }>()

  const { t } = useI18n()
  const deletionVisible = ref(false)
  const form = reactive({
    username: ''
  })
  const renaming = ref(false)

  watch(
    () => props.detail.username,
    (username) => {
      form.username = username || ''
    },
    { immediate: true }
  )

  const rules = reactive<FormRules>({
    username: [
      {
        required: true,
        message: t('pages.system.accountCenter.account.username.validation.required'),
        trigger: 'blur'
      },
      {
        min: 3,
        max: 12,
        message: t('pages.system.accountCenter.account.username.validation.length'),
        trigger: 'blur'
      }
    ]
  })

  const renameAccount = async () => {
    if (renaming.value) return

    const username = form.username.trim()
    if (!username) {
      ElMessage.warning(t('pages.system.accountCenter.account.username.validation.required'))
      return
    }
    if (username.length < 3 || username.length > 12) {
      ElMessage.warning(t('pages.system.accountCenter.account.username.validation.length'))
      return
    }
    if (username === (props.detail.username || '')) {
      return
    }

    renaming.value = true
    try {
      await fetchUpdateAccountUsername({ username })
      ElMessage.success(t('pages.system.accountCenter.account.username.success'))
      emit('username-updated', username)
    } finally {
      renaming.value = false
    }
  }

  const openDeletion = () => {
    if (props.detail.emailMfaEnabled || props.detail.totpMfaEnabled) {
      ElMessageBox.alert(
        t('pages.system.accountCenter.account.deletion.mfaBlockingDialog.content'),
        t('pages.system.accountCenter.account.deletion.mfaBlockingDialog.title'),
        {
          confirmButtonText: t('pages.system.accountCenter.account.deletion.mfaBlockingDialog.confirm'),
          dangerouslyUseHTMLString: true
        }
      )
      return
    }
    if (props.detail.pendingDeletion) {
      ElMessage.warning(t('pages.system.accountCenter.account.deletion.pendingTip'))
      return
    }
    deletionVisible.value = true
  }
</script>

<template>
  <div class="account-settings-page">
    <header class="border-b border-g-300 pb-3">
      <h1 class="text-xl font-medium leading-8 text-g-900">
        {{ t('pages.system.accountCenter.account.title') }}
      </h1>
      <p class="mt-1 text-sm text-g-600">
        {{ t('pages.system.accountCenter.account.desc') }}
      </p>
    </header>

    <section class="setting-section block-section">
      <div class="rename-panel">
        <h2 class="setting-title">{{ t('pages.system.accountCenter.account.username.title') }}</h2>
        <ElForm :model="form" :rules="rules" label-position="top" class="rename-form">
          <div class="rename-panel__row">
            <ElFormItem :label="t('pages.system.accountCenter.account.username.label')" prop="username">
              <ElInput
                v-model="form.username"
                :maxlength="12"
                show-word-limit
                class="rename-panel__input"
              />
            </ElFormItem>

            <div class="rename-panel__action">
              <ElButton type="primary" v-ripple @click="renameAccount">
                {{ t('pages.system.accountCenter.account.username.action') }}
              </ElButton>
            </div>
          </div>
        </ElForm>
      </div>
    </section>

    <section class="danger-zone">
      <div class="danger-zone__subhead">
        <h2 class="setting-title danger-zone__title">
          {{ t('pages.system.accountCenter.account.deletion.title') }}
        </h2>
      </div>
      <p class="danger-zone__desc">
        {{ t('pages.system.accountCenter.account.deletion.desc') }}
      </p>
      <div class="danger-zone__actions">
        <div v-if="detail.pendingDeletion" class="mt-2 text-sm text-danger">
          {{
            t('pages.system.accountCenter.account.deletion.pendingStatus', {
              time: detail.deletionExpiresAt || '-'
            })
          }}
        </div>
        <ElButton type="danger" @click="openDeletion">
          {{ t('pages.system.accountCenter.account.deletion.button') }}
        </ElButton>
      </div>
    </section>

    <DeleteAccountDialog
      v-model:visible="deletionVisible"
      :username="detail.username || ''"
      :security-email="detail.securityEmail"
      :email-mfa-enabled="Boolean(detail.emailMfaEnabled)"
      :totp-mfa-enabled="Boolean(detail.totpMfaEnabled)"
      @success="emit('deletion-requested')"
    />
  </div>
</template>

<style scoped>
  .account-settings-page {
    padding-bottom: 80px;
  }

  .setting-section {
    border-bottom: 1px solid var(--velox-gray-300);
    padding: 24px 0;
  }

  .rename-panel {
    width: 33.3333%;
    max-width: 420px;
  }

  .block-section {
    display: block;
  }

  .setting-title {
    color: var(--velox-text-color);
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
  }

  .rename-form {
    margin-top: 16px;
  }

  .rename-form :deep(.el-form-item) {
    margin-bottom: 0;
    min-width: 0;
    flex: 1;
  }

  .rename-form :deep(.el-form-item__label) {
    margin-bottom: 6px;
    color: var(--velox-text-color);
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
  }

  .rename-panel__input {
    width: 100%;
  }

  .rename-panel__row {
    display: flex;
    align-items: flex-end;
    gap: 16px;
  }

  .rename-panel__action {
    flex: none;
  }

  .danger-zone {
    padding: 24px 0 8px;
  }

  .danger-zone__subhead {
    margin-bottom: 4px;
  }

  .danger-zone__title {
    color: var(--el-color-danger);
  }

  .danger-zone__desc {
    color: var(--velox-text-color);
    font-size: 14px;
    line-height: 22px;
  }

  .danger-zone__actions {
    margin-top: 20px;
  }

  @media only screen and (width <= 640px) {
    .rename-panel {
      width: 100%;
      max-width: none;
    }

    .rename-panel__row {
      display: block;
    }

    .rename-panel__action {
      margin-top: 12px;
    }
  }

  @media only screen and (641px <= width <= 1080px) {
    .rename-panel {
      width: 50%;
    }
  }
</style>
