<template>
  <div class="velox-full-height verification-settings-page">
    <ElCard class="velox-table-card">
      <div class="page-header">
        <h2 class="page-title">{{ t('pages.settings.verificationSettings.title') }}</h2>
        <p class="page-desc">{{ t('pages.settings.verificationSettings.description') }}</p>
      </div>

      <div v-loading="loading" class="vs-list">
        <div v-for="scene in scenes" :key="scene.sceneKey" class="vs-section">
          <div class="vs-section__head">
            <div class="vs-section__title">
              {{ t(`pages.settings.verificationSettings.scenes.${scene.sceneKey}.name`) }}
            </div>
            <ElSwitch
              v-model="scene.enabled"
              :disabled="!canEdit || loading"
              @change="() => handleSave(scene)"
            />
          </div>
          <div class="vs-section__desc">
            {{ t(`pages.settings.verificationSettings.scenes.${scene.sceneKey}.description`) }}
          </div>
          <div class="vs-section__fields">
            <div class="vs-field">
              <span class="vs-field__label">
                {{ t('pages.settings.verificationSettings.columns.maxAttempts') }}
              </span>
              <ElInputNumber
                v-model="scene.maxAttempts"
                :min="1"
                :step="1"
                :disabled="!canEdit || loading"
                controls-position="right"
                @change="() => handleSave(scene)"
              />
              <span class="vs-field__unit">
                {{ t('pages.settings.verificationSettings.unit.times') }}
              </span>
            </div>
            <div class="vs-field">
              <span class="vs-field__label">
                {{ t('pages.settings.verificationSettings.columns.recoverySeconds') }}
              </span>
              <ElInputNumber
                v-model="scene.recoverySeconds"
                :min="1"
                :step="10"
                :disabled="!canEdit || loading"
                controls-position="right"
                @change="() => handleSave(scene)"
              />
              <span class="vs-field__unit">
                {{ t('pages.settings.verificationSettings.unit.seconds') }}
              </span>
            </div>
            <div class="vs-field">
              <span class="vs-field__label">
                {{ t('pages.settings.verificationSettings.columns.dimensions') }}
              </span>
              <ElCheckboxGroup
                v-model="scene.dimensions"
                :disabled="!canEdit || loading"
                @change="() => handleSave(scene)"
              >
                <ElCheckbox
                  v-for="d in DIMENSIONS"
                  :key="d"
                  :value="d"
                  :label="d"
                >
                  {{ t(`pages.settings.verificationSettings.dimensions.${d}`) }}
                </ElCheckbox>
              </ElCheckboxGroup>
            </div>
          </div>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { ElMessage } from 'element-plus'
  import { useAuth } from '@/hooks/core/useAuth'
  import {
    fetchGetVerificationSettings,
    fetchUpdateVerificationScene,
    type VerificationPolicyConfig
  } from '@/api/settings'

  defineOptions({ name: 'VerificationSettings' })

  const { t } = useI18n()
  const { hasAuth } = useAuth()

  const DIMENSIONS = ['account', 'ip'] as const

  interface SceneRow {
    sceneKey: string
    enabled: boolean
    maxAttempts: number
    recoverySeconds: number
    dimensions: string[]
  }

  const loading = ref(false)
  const scenes = reactive<SceneRow[]>([])
  // 每个场景的最近成功快照，保存失败时按场景回滚
  const snapshots: Record<string, SceneRow> = {}

  const canEdit = computed(() => hasAuth('settings:verification-settings:update'))

  function toRow(data: VerificationPolicyConfig): SceneRow {
    const dimensions: string[] = []
    if (data.limitByAccount) dimensions.push('account')
    if (data.limitByIp) dimensions.push('ip')
    return {
      sceneKey: data.sceneKey,
      enabled: data.enabled,
      maxAttempts: data.maxAttempts,
      recoverySeconds: data.recoverySeconds,
      dimensions
    }
  }

  function cloneRow(row: SceneRow): SceneRow {
    return { ...row, dimensions: [...row.dimensions] }
  }

  function revert(scene: SceneRow) {
    const snap = snapshots[scene.sceneKey]
    if (!snap) return
    scene.enabled = snap.enabled
    scene.maxAttempts = snap.maxAttempts
    scene.recoverySeconds = snap.recoverySeconds
    scene.dimensions = [...snap.dimensions]
  }

  async function loadConfig() {
    loading.value = true
    try {
      const list = await fetchGetVerificationSettings()
      scenes.splice(0, scenes.length, ...list.map(toRow))
      scenes.forEach((s) => {
        snapshots[s.sceneKey] = cloneRow(s)
      })
    } catch {
      ElMessage.error(t('pages.settings.verificationSettings.loadFailed'))
    } finally {
      loading.value = false
    }
  }

  // 乐观更新：控件已反映新值，成功提交快照，失败回滚该场景
  async function handleSave(scene: SceneRow) {
    if (scene.maxAttempts == null || scene.recoverySeconds == null) {
      revert(scene)
      ElMessage.warning(t('pages.settings.verificationSettings.invalidValue'))
      return
    }
    try {
      await fetchUpdateVerificationScene(scene.sceneKey, {
        enabled: scene.enabled,
        maxAttempts: scene.maxAttempts,
        recoverySeconds: scene.recoverySeconds,
        limitByAccount: scene.dimensions.includes('account'),
        limitByIp: scene.dimensions.includes('ip')
      })
      snapshots[scene.sceneKey] = cloneRow(scene)
      ElMessage.success(t('pages.settings.verificationSettings.saveSuccess'))
    } catch {
      revert(scene)
    }
  }

  onMounted(loadConfig)
</script>

<style lang="scss" scoped>
  .verification-settings-page {
    // 该页为普通表单内容（非 VeloxTable 自管滚动），需让卡片体在内容超高时滚动，
    // 覆盖 .velox-table-card .el-card__body 的 overflow:hidden（仅作用于本页）。
    :deep(.el-card__body) {
      overflow-y: auto;
    }

    .page-header {
      margin-bottom: 20px;

      .page-title {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: 600;
      }

      .page-desc {
        margin: 0;
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }
    }

    .vs-section {
      padding: 18px 0;
      border-bottom: 1px solid var(--el-border-color-lighter);

      &:last-child {
        border-bottom: none;
      }

      &__head {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      &__title {
        font-size: 15px;
        font-weight: 500;
      }

      &__desc {
        margin: 6px 0 14px;
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }

      &__fields {
        display: flex;
        flex-wrap: wrap;
        gap: 24px 36px;
        align-items: center;
      }
    }

    .vs-field {
      display: flex;
      align-items: center;
      gap: 10px;

      &__label {
        font-size: 13px;
        color: var(--el-text-color-regular);
      }

      &__unit {
        font-size: 13px;
        color: var(--el-text-color-secondary);
      }
    }
  }
</style>
