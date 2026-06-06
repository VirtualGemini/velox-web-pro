<template>
  <ElDialog
    v-model="innerVisible"
    :title="t('pages.config.mailTemplate.preview.title')"
    :width="dialogWidth"
    align-center
    class="mail-preview-dialog"
    append-to-body
  >
    <div class="mail-preview">
      <div class="mail-preview__client">
        <div class="mail-preview__meta">
          <div class="mail-preview__subject">{{ renderedSubject || '—' }}</div>
          <div class="mail-preview__row">
            <span class="mail-preview__label">{{ t('pages.config.mailTemplate.preview.from') }}</span>
            <span class="mail-preview__value"
              >{{ sampleVars.appName }} &lt;{{ props.metadata.previewSample.fromAddress }}&gt;</span
            >
          </div>
          <div class="mail-preview__row">
            <span class="mail-preview__label">{{ t('pages.config.mailTemplate.preview.to') }}</span>
            <span class="mail-preview__value"
              >{{ sampleVars.username }} &lt;{{ props.metadata.previewSample.toAddress }}&gt;</span
            >
          </div>
        </div>
        <!-- sandbox 不开放 allow-scripts/allow-same-origin，配合 srcdoc 隔绝脚本与网络访问，封死预览 XSS -->
        <iframe
          class="mail-preview__body"
          :srcdoc="previewHtml"
          sandbox=""
          referrerpolicy="no-referrer"
          title="mail-preview"
        ></iframe>
      </div>
    </div>

    <template #footer>
      <ElButton @click="innerVisible = false">{{ t('pages.config.mailTemplate.preview.close') }}</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useWindowSize } from '@vueuse/core'
  import { buildPreviewVariables } from '../metadata'
  import type { MailTemplateMetadata } from '@/api/mailTemplate'

  const props = withDefaults(
    defineProps<{
      visible: boolean
      metadata: MailTemplateMetadata
      type: string
      subject: string
      content: string
    }>(),
    {
      type: '',
      subject: '',
      content: ''
    }
  )

  const emit = defineEmits<{ 'update:visible': [value: boolean] }>()

  const { t, locale } = useI18n()
  const { width } = useWindowSize()

  const innerVisible = computed({
    get: () => props.visible,
    set: (value: boolean) => emit('update:visible', value)
  })

  const dialogWidth = computed(() => (width.value <= 640 ? 'calc(100vw - 24px)' : '760px'))

  const VARIABLE_PATTERN = /\{\{\s*([a-zA-Z0-9_]+)\s*\}\}/g

  const sampleVars = computed(() => buildPreviewVariables(props.metadata, props.type))

  function escapeHtml(value: string) {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;')
  }

  // 与后端渲染口径一致：正文对变量值做 HTML 转义，主题保持原值。
  function substitute(template: string, escape: boolean) {
    const vars = sampleVars.value
    return (template || '').replace(VARIABLE_PATTERN, (_match, key: string) => {
      const value = Object.prototype.hasOwnProperty.call(vars, key) ? vars[key] : ''
      return escape ? escapeHtml(value) : value
    })
  }

  const renderedSubject = computed(() => substitute(props.subject, false))

  const previewHtml = computed(() => {
    const body = substitute(props.content, true)
    return [
      '<!DOCTYPE html>',
      `<html lang="${locale.value}">`,
      '<head><meta charset="utf-8" />',
      '<meta name="viewport" content="width=device-width, initial-scale=1" />',
      '<style>html,body{margin:0;padding:0;}',
      'body{padding:20px;background:#f3f4f6;',
      "font-family:Arial,Helvetica,sans-serif;color:#333;}",
      '.mail-preview-card{max-width:600px;margin:0 auto;background:#fff;',
      'border-radius:8px;padding:24px;box-shadow:0 1px 4px rgba(0,0,0,0.08);}',
      '</style></head>',
      '<body><div class="mail-preview-card">',
      body,
      '</div></body></html>'
    ].join('')
  })
</script>

<style scoped lang="scss">
  .mail-preview {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .mail-preview__client {
    overflow: hidden;
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
  }

  .mail-preview__meta {
    padding: 14px 16px;
    background: var(--el-fill-color-lighter);
    border-bottom: 1px solid var(--el-border-color);
  }

  .mail-preview__subject {
    margin-bottom: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    word-break: break-word;
  }

  .mail-preview__row {
    display: flex;
    gap: 8px;
    font-size: 13px;
    line-height: 1.8;
  }

  .mail-preview__label {
    flex-shrink: 0;
    width: 52px;
    color: var(--el-text-color-secondary);
  }

  .mail-preview__value {
    color: var(--el-text-color-regular);
    word-break: break-word;
  }

  .mail-preview__body {
    display: block;
    width: 100%;
    height: 460px;
    background: #f3f4f6;
    border: 0;
  }

  @media (width <= 640px) {
    .mail-preview__body {
      height: 60vh;
    }
  }
</style>
