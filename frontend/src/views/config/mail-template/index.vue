<template>
  <div
    v-loading="pageLoading"
    class="mail-template-page velox-full-height"
    :element-loading-text="t('common.loading')"
  >
    <VeloxSearchBar
      v-show="showSearchBar && activeTab === 'list'"
      v-model="searchForm"
      :items="searchItems"
      @search="handleSearch"
      @reset="handleReset"
    />

    <Transition :name="transitionDirection" mode="out-in">
      <ElCard
        v-if="activeTab === 'list'"
        key="list"
        class="velox-table-card"
        :style="{ 'margin-top': showSearchBar ? '12px' : '0' }"
      >
        <VeloxTableHeader
          v-model:columns="columnChecks"
          v-model:showSearchBar="showSearchBar"
          @refresh="refreshData"
        >
          <template #left>
            <ElSpace wrap>
              <ElButton
                v-if="canAccess('system:mail-template:create')"
                :disabled="pageLoading"
                @click="openCreateDialog"
                v-ripple
              >
                {{ t('pages.config.mailTemplate.actions.add') }}
              </ElButton>
              <ElButton
                v-if="canAccess('system:mail-template:delete')"
                class="velox-batch-delete"
                type="danger"
                plain
                :disabled="pageLoading || selectedRows.length === 0 || hasSelectedSystemTemplate"
                @click="handleBatchDelete"
                v-ripple
              >
                {{ t('common.batchDelete') }}
              </ElButton>
            </ElSpace>
          </template>
        </VeloxTableHeader>

        <VeloxTable
          ref="veloxTableRef"
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          @selection-change="handleSelectionChange"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </ElCard>

      <MailEditor
        v-else-if="activeTab === 'editor' && editingTemplate"
        ref="mailEditorRef"
        :key="editingTemplate.id"
        class="mail-template-editor-view"
        :template="editingTemplate"
        :metadata="metadata"
        @back="onEditorBack"
        @saved="onEditorSaved"
      />
    </Transition>

    <ElDialog
      v-model="dialogVisible"
      :title="
        dialogMode === 'create'
          ? t('pages.config.mailTemplate.form.titles.create')
          : t('pages.config.mailTemplate.form.titles.edit')
      "
      :width="dialogWidth"
      align-center
      class="mail-template-dialog"
      :close-on-click-modal="false"
      @close="handleDialogClose"
      @closed="handleDialogClosed"
    >
      <ElForm
        ref="formRef"
        v-loading="submitLoading"
        :model="formModel"
        :rules="formRules"
        :label-width="formLabelWidth"
        :element-loading-text="t('common.loading')"
        label-position="right"
        class="mail-template-form"
      >
        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.mailTemplate.form.fields.name')" prop="name">
              <ElInput
                v-model="formModel.name"
                maxlength="100"
                :placeholder="t('pages.config.mailTemplate.form.placeholders.name')"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem prop="sendType">
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailTemplate.form.fields.sendType')"
                  :tooltip="t('pages.config.mailTemplate.form.tooltips.sendType')"
                />
              </template>
              <ElSelect
                v-model="formModel.sendType"
                :disabled="dialogMode === 'edit'"
                :placeholder="t('pages.config.mailTemplate.form.placeholders.sendType')"
              >
                <ElOption
                  v-for="option in sendTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.mailTemplate.form.fields.templateType')">
              <ElSelect
                v-model="formModel.templateType"
                disabled
                :placeholder="t('pages.config.mailTemplate.form.placeholders.templateType')"
              >
                <ElOption
                  v-for="option in templateTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem>
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailTemplate.form.fields.enabled')"
                  :tooltip="t('pages.config.mailTemplate.form.tooltips.enabled')"
                />
              </template>
              <ElSwitch v-model="formModel.enabled" />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem :label="t('pages.config.mailTemplate.form.fields.subject')" prop="subject">
          <ElInput
            v-model="formModel.subject"
            maxlength="255"
            :placeholder="t('pages.config.mailTemplate.form.placeholders.subject')"
          />
        </ElFormItem>

        <ElFormItem :label="t('pages.config.mailTemplate.form.fields.remark')">
          <ElInput
            v-model="formModel.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            :placeholder="t('pages.config.mailTemplate.form.placeholders.remark')"
          />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton :disabled="submitLoading" @click="handleDialogClose">
          {{ t('common.cancel') }}
        </ElButton>
        <ElButton
          v-if="dialogMode === 'edit'"
          type="primary"
          :disabled="submitLoading"
          @click="handleSubmitAndEditMail"
        >
          {{ t('pages.config.mailTemplate.actions.editMail') }}
        </ElButton>
        <ElButton type="primary" :disabled="submitLoading" @click="handleSubmit">
          {{ t('table.form.submit') }}
        </ElButton>
      </template>
    </ElDialog>

    <MailPreviewDialog
      v-model:visible="previewVisible"
      :metadata="metadata"
      :type="previewPayload.sendType"
      :subject="previewPayload.subject"
      :content="previewPayload.content"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import {
    ElDropdown,
    ElDropdownItem,
    ElDropdownMenu,
    ElIcon,
    ElMessage,
    ElMessageBox,
    ElSwitch,
    ElTag,
    ElTooltip,
    type FormInstance,
    type FormRules
  } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { useWindowSize } from '@vueuse/core'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import MailPreviewDialog from './components/MailPreviewDialog.vue'
  import MailEditor from './components/MailEditor.vue'
  import {
    type MailTemplate,
    type MailTemplateKind,
    type MailTemplateMetadata,
    type MailTemplateSaveCommand,
    fetchMailTemplateCreate,
    fetchMailTemplateDelete,
    fetchMailTemplateDeleteBatch,
    fetchMailTemplateGet,
    fetchMailTemplateMetadata,
    fetchMailTemplatePage,
    fetchMailTemplateUpdate,
    fetchMailTemplateUpdateEnabled
  } from '@/api/mailTemplate'
  import { createEmptyMailTemplateMetadata } from './metadata'

  defineOptions({ name: 'EmailTemplate' })

  type DialogMode = 'create' | 'edit'

  interface MailTemplateFormModel {
    name: string
    sendType: string
    templateType: MailTemplateKind
    subject: string
    enabled: boolean
    remark: string
  }

  type MailTemplateSearchForm = {
    name?: string
    sendType?: string
    templateType?: MailTemplateKind
    enabled?: number
    remark?: string
    createTimeRange?: [string, string]
    updateTimeRange?: [string, string]
  }

  const { hasAuth } = useAuth()
  const route = useRoute()
  const router = useRouter()
  const { t } = useI18n()
  const { width } = useWindowSize()

  const isMobile = computed(() => width.value <= 640)
  const dialogWidth = computed(() => (isMobile.value ? 'calc(100vw - 24px)' : '880px'))
  const formHalfSpan = computed(() => (isMobile.value ? 24 : 12))
  const formLabelWidth = computed(() => (isMobile.value ? '120px' : 'auto'))
  const metadata = ref<MailTemplateMetadata>(createEmptyMailTemplateMetadata())

  const sendTypeOptions = computed(() =>
    metadata.value.sendTypes.map((value) => ({
      label: t(`pages.config.mailTemplate.types.${value}`),
      value
    }))
  )
  const templateTypeOptions = computed(() =>
    metadata.value.templateTypes.map((value) => ({
      label: value === 'SYSTEM' ? t('common.status.builtin') : t('common.status.custom'),
      value
    }))
  )
  function sendTypeLabel(code: string) {
    return t(`pages.config.mailTemplate.types.${code}`)
  }

  function templateTypeLabel(kind: string) {
    if (kind === 'SYSTEM') return t('common.status.builtin')
    if (kind === 'CUSTOM') return t('common.status.custom')
    return kind
  }

  const searchForm = ref<MailTemplateSearchForm>({
    name: undefined,
    sendType: undefined,
    templateType: undefined,
    enabled: undefined,
    remark: undefined,
    createTimeRange: undefined,
    updateTimeRange: undefined
  })
  const showSearchBar = ref(false)

  const searchItems = computed(() => [
    {
      label: 'pages.config.mailTemplate.search.name',
      key: 'name',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: 'pages.config.mailTemplate.search.sendType',
      key: 'sendType',
      type: 'select',
      props: { clearable: true, options: sendTypeOptions.value }
    },
    {
      label: 'pages.config.mailTemplate.search.templateType',
      key: 'templateType',
      type: 'select',
      props: { clearable: true, options: templateTypeOptions.value }
    },
    {
      label: 'pages.config.mailTemplate.search.enabled',
      key: 'enabled',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: 'common.status.enabled', value: 1 },
          { label: 'common.status.disabled', value: 0 }
        ]
      }
    },
    {
      label: 'pages.config.mailTemplate.search.remark',
      key: 'remark',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: 'pages.config.mailTemplate.search.createTimeRange',
      key: 'createTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.config.mailTemplate.search.placeholders.startTime',
        endPlaceholder: 'pages.config.mailTemplate.search.placeholders.endTime',
        rangeSeparator: 'pages.config.mailTemplate.search.rangeSeparator'
      }
    },
    {
      label: 'pages.config.mailTemplate.search.updateTimeRange',
      key: 'updateTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.config.mailTemplate.search.placeholders.startTime',
        endPlaceholder: 'pages.config.mailTemplate.search.placeholders.endTime',
        rangeSeparator: 'pages.config.mailTemplate.search.rangeSeparator'
      }
    }
  ])

  const dialogVisible = ref(false)
  const dialogMode = ref<DialogMode>('create')
  const submitLoading = ref(false)
  const currentEditId = ref('')
  const veloxTableRef = ref()
  const selectedRows = ref<MailTemplate[]>([])
  const formRef = ref<FormInstance>()
  const formModel = reactive(createDefaultForm())
  const formContent = ref('')
  const mailEditorRef = ref<InstanceType<typeof MailEditor>>()

  // ── 编辑器模式：URL query + sessionStorage 共同保持当前标签页内的编辑状态 ──
  const EDITOR_QUERY_KEY = 'editorId'
  const EDITOR_SESSION_KEY = 'velox:mail-template:editor-session'
  const activeTab = ref<'list' | 'editor'>('list')
  const transitionDirection = ref<'slide-left' | 'slide-right'>('slide-left')
  const editingTemplate = ref<(MailTemplate & { content: string }) | null>(null)

  const previewVisible = ref(false)
  const previewPayload = reactive<{
    sendType: string
    subject: string
    content: string
  }>({
    sendType: '',
    subject: '',
    content: ''
  })

  const routeAuthMarks = computed(() => {
    const authList = Array.isArray(route.meta.authList) ? route.meta.authList : []
    return authList
      .map((item) =>
        item && typeof item === 'object'
          ? String((item as Record<string, unknown>).authMark || '')
          : ''
      )
      .filter(Boolean)
  })

  const hasMailTemplateAuthBinding = computed(() =>
    routeAuthMarks.value.some((auth) => auth.startsWith('system:mail-template:'))
  )
  const hasSelectedSystemTemplate = computed(() =>
    selectedRows.value.some((row) => row.templateType === 'SYSTEM')
  )

  const canAccess = (auth: string) => {
    return hasMailTemplateAuthBinding.value ? hasAuth(auth) : true
  }

  const LabelTooltip = defineComponent({
    props: {
      label: { type: String, required: true },
      tooltip: { type: String, required: true }
    },
    setup(props) {
      return () =>
        h('span', { class: 'flex items-center' }, [
          h('span', props.label),
          h(ElTooltip, { content: props.tooltip, placement: 'top' }, () =>
            h(ElIcon, { class: 'ml-0.5 cursor-help text-[var(--el-text-color-secondary)]' }, () =>
              h(QuestionFilled)
            )
          )
        ])
    }
  })

  const formRules = reactive<FormRules<MailTemplateFormModel>>({
    name: [
      {
        required: true,
        message: t('pages.config.mailTemplate.messages.enterName'),
        trigger: 'blur'
      }
    ],
    sendType: [
      {
        required: true,
        message: t('pages.config.mailTemplate.messages.selectSendType'),
        trigger: 'change'
      }
    ],
    subject: [
      {
        required: true,
        message: t('pages.config.mailTemplate.messages.enterSubject'),
        trigger: 'blur'
      }
    ]
  })

  const pageLoadingCount = ref(0)
  const pageLoading = computed(() => pageLoadingCount.value > 0)

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData: getTableData,
    replaceSearchParams,
    resetSearchParams: resetTableSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData: refreshTableData
  } = useTable({
    core: {
      apiFn: fetchMailTemplatePage,
      apiParams: {
        page: 1,
        size: 20
      },
      paginationKey: {
        current: 'page',
        size: 'size'
      },
      columnsFactory: () => [
        { type: 'selection', width: 52 },
        { type: 'index', label: t('table.column.index'), minWidth: 70 },
        {
          prop: 'name',
          label: 'pages.config.mailTemplate.columns.name',
          minWidth: 180,
          showOverflowTooltip: true
        },
        {
          prop: 'sendType',
          label: 'pages.config.mailTemplate.columns.sendType',
          minWidth: 180,
          showOverflowTooltip: true,
          formatter: (row: MailTemplate) => sendTypeLabel(row.sendType || row.type)
        },
        {
          prop: 'templateType',
          label: 'pages.config.mailTemplate.columns.templateType',
          minWidth: 120,
          align: 'center',
          formatter: (row: MailTemplate) =>
            h(ElTag, { type: row.templateType === 'SYSTEM' ? 'danger' : 'info' }, () =>
              templateTypeLabel(row.templateType)
            )
        },
        {
          prop: 'subject',
          label: 'pages.config.mailTemplate.columns.subject',
          minWidth: 200,
          showOverflowTooltip: true,
          formatter: (row: MailTemplate) => row.subject || '-'
        },
        {
          prop: 'enabled',
          label: 'pages.config.mailTemplate.columns.enabled',
          minWidth: 100,
          formatter: (row: MailTemplate) =>
            h(ElSwitch, {
              modelValue: row.enabled === 1,
              disabled: pageLoading.value || !canAccess('system:mail-template:update'),
              'onUpdate:modelValue': (val: string | number | boolean) =>
                handleEnabledChange(row, !!val)
            })
        },
        {
          prop: 'remark',
          label: 'pages.config.mailTemplate.columns.remark',
          minWidth: 160,
          showOverflowTooltip: true,
          formatter: (row: MailTemplate) => row.remark || '-'
        },
        {
          prop: 'createTime',
          label: 'pages.config.mailTemplate.columns.createTime',
          minWidth: 180
        },
        {
          prop: 'updateTime',
          label: 'pages.config.mailTemplate.columns.updateTime',
          minWidth: 180
        },
        {
          prop: 'operation',
          label: 'pages.config.mailTemplate.columns.operation',
          width: 136,
          fixed: 'right',
          align: 'center',
          formatter: (row: MailTemplate) => renderOperationButtons(row)
        }
      ]
    }
  })

  const EMPTY_HTML =
    '<!DOCTYPE html><html lang="en"><head><meta charset="utf-8" /><meta name="viewport" content="width=device-width, initial-scale=1" /></head><body><div style="max-width:600px;margin:0 auto;padding:20px;"></div></body></html>'

  async function withPageLoading<T>(runner: () => Promise<T>) {
    pageLoadingCount.value += 1
    try {
      return await runner()
    } finally {
      pageLoadingCount.value = Math.max(0, pageLoadingCount.value - 1)
    }
  }

  async function refreshData() {
    await withPageLoading(() => refreshTableData())
  }

  function createDefaultForm(): MailTemplateFormModel {
    return {
      name: '',
      sendType: '',
      templateType: 'CUSTOM',
      subject: 'Velox Web Pro',
      enabled: true,
      remark: ''
    }
  }

  function renderOperationButtons(row: MailTemplate) {
    const moreItems: ReturnType<typeof h>[] = []

    if (canAccess('system:mail-template:query')) {
      moreItems.push(
        h(ElDropdownItem, { onClick: () => openPreviewFromRow(row) }, () =>
          t('pages.config.mailTemplate.actions.preview')
        )
      )
    }

    if (canAccess('system:mail-template:create')) {
      moreItems.push(
        h(ElDropdownItem, { onClick: () => openCopyDialog(row) }, () =>
          t('pages.config.mailTemplate.actions.copy')
        )
      )
    }

    return h(
      'div',
      { class: 'mail-template-operation-buttons flex items-center justify-center' },
      [
        canAccess('system:mail-template:update')
          ? h(VeloxButtonTable, { type: 'edit', onClick: () => openEditDialog(row) })
          : null,
        canAccess('system:mail-template:delete') && row.templateType !== 'SYSTEM'
          ? h(VeloxButtonTable, { type: 'delete', onClick: () => handleDelete(row) })
          : null,
        moreItems.length > 0
          ? h(
              ElDropdown,
              { trigger: 'click' },
              {
                default: () => h(VeloxButtonTable, { type: 'more' }),
                dropdown: () => h(ElDropdownMenu, {}, () => moreItems)
              }
            )
          : null
      ].filter(Boolean)
    )
  }

  function resolveTemplateId(row?: Pick<MailTemplate, 'id'>) {
    const templateId = row?.id?.trim()
    if (!templateId) {
      ElMessage.error(t('pages.config.mailTemplate.messages.missingId'))
      return null
    }
    return templateId
  }

  function fillForm(detail?: MailTemplate) {
    Object.assign(formModel, createDefaultForm())
    formContent.value = EMPTY_HTML
    if (!detail) return

    Object.assign(formModel, {
      name: detail.name || '',
      sendType: detail.sendType || detail.type || '',
      templateType: detail.templateType || 'CUSTOM',
      subject: detail.subject || '',
      enabled: detail.enabled === 1,
      remark: detail.remark || ''
    })
    formContent.value = detail.content || ''
  }

  async function openCreateDialog() {
    if (metadata.value.sendTypes.length === 0) {
      await loadMetadata()
    }
    dialogMode.value = 'create'
    currentEditId.value = ''
    fillForm()
    if (metadata.value.sendTypes.length > 0) {
      formModel.sendType = metadata.value.sendTypes[0]
    }
    dialogVisible.value = true
  }

  async function openEditDialog(row: MailTemplate) {
    const templateId = resolveTemplateId(row)
    if (!templateId) return

    await withPageLoading(async () => {
      const detail = await fetchMailTemplateGet(templateId)
      dialogMode.value = 'edit'
      currentEditId.value = templateId
      fillForm(detail)
      dialogVisible.value = true
    })
  }

  // 复制：拉取完整内容预填新增弹窗，但清空名称、强制停用，由名称唯一约束促使改名。
  async function openCopyDialog(row: MailTemplate) {
    const templateId = resolveTemplateId(row)
    if (!templateId) return

    await withPageLoading(async () => {
      const detail = await fetchMailTemplateGet(templateId)
      dialogMode.value = 'create'
      currentEditId.value = ''
      fillForm(detail)
      formModel.name = ''
      formModel.templateType = 'CUSTOM'
      formModel.enabled = false
      dialogVisible.value = true
    })
  }

  function handleDialogClose() {
    dialogVisible.value = false
    submitLoading.value = false
  }

  // 关闭动画结束后再重置表单，避免关闭过程中清空内容导致闪烁。
  function handleDialogClosed() {
    formRef.value?.resetFields()
    fillForm()
  }

  function buildPayload(): MailTemplateSaveCommand {
    return {
      name: formModel.name.trim(),
      sendType: formModel.sendType,
      templateType: formModel.templateType,
      subject: formModel.subject.trim(),
      content: formContent.value || EMPTY_HTML,
      enabled: formModel.enabled ? 1 : 0,
      remark: formModel.remark.trim() || undefined
    }
  }

  function getEditorIdFromQuery() {
    const value = route.query[EDITOR_QUERY_KEY]
    return Array.isArray(value) ? value[0] : value
  }

  function syncEditorQuery(templateId: string) {
    if (getEditorIdFromQuery() === templateId) return
    void router.replace({
      query: {
        ...route.query,
        [EDITOR_QUERY_KEY]: templateId
      }
    })
  }

  function clearEditorQuery() {
    if (!(EDITOR_QUERY_KEY in route.query)) return
    const query = { ...route.query }
    delete query[EDITOR_QUERY_KEY]
    void router.replace({ query })
  }

  function persistEditorSession(templateId: string) {
    window.sessionStorage.setItem(
      EDITOR_SESSION_KEY,
      JSON.stringify({
        templateId,
        updatedAt: Date.now()
      })
    )
  }

  function readEditorSession() {
    try {
      const raw = window.sessionStorage.getItem(EDITOR_SESSION_KEY)
      if (!raw) return ''
      const session = JSON.parse(raw) as { templateId?: string }
      return session.templateId || ''
    } catch {
      return ''
    }
  }

  function clearEditorSession() {
    window.sessionStorage.removeItem(EDITOR_SESSION_KEY)
  }

  async function openEditorById(templateId: string, syncQuery = true, showLoading = true) {
    const openEditor = async () => {
      const detail = await fetchMailTemplateGet(templateId)
      editingTemplate.value = detail as MailTemplate & { content: string }
      transitionDirection.value = 'slide-left'
      activeTab.value = 'editor'
      persistEditorSession(templateId)
      if (syncQuery) syncEditorQuery(templateId)
    }

    if (showLoading) {
      await withPageLoading(openEditor)
    } else {
      await openEditor()
    }
  }

  async function restoreEditorState() {
    const templateId = getEditorIdFromQuery() || readEditorSession()
    if (!templateId) return

    try {
      await openEditorById(templateId, false, false)
    } catch {
      editingTemplate.value = null
      activeTab.value = 'list'
      clearEditorQuery()
      clearEditorSession()
    }
  }

  async function handleSubmit() {
    if (!formRef.value) return
    try {
      await formRef.value.validate()
    } catch {
      return
    }

    submitLoading.value = true
    try {
      const payload = buildPayload()
      if (dialogMode.value === 'create') {
        await fetchMailTemplateCreate(payload)
        ElMessage.success(t('pages.config.mailTemplate.messages.createSuccess'))
      } else {
        payload.id = currentEditId.value
        await fetchMailTemplateUpdate(payload)
        ElMessage.success(t('pages.config.mailTemplate.messages.updateSuccess'))
      }
      handleDialogClose()
      await refreshData()
    } finally {
      submitLoading.value = false
    }
  }

  // 保存基本信息并打开邮件编辑器
  async function handleSubmitAndEditMail() {
    if (!formRef.value) return
    try {
      await formRef.value.validate()
    } catch {
      return
    }

    submitLoading.value = true
    try {
      const payload = buildPayload()
      let templateId: string

      if (dialogMode.value === 'create') {
        templateId = await fetchMailTemplateCreate(payload)
      } else {
        payload.id = currentEditId.value
        await fetchMailTemplateUpdate(payload)
        templateId = currentEditId.value
      }

      handleDialogClose()
      await openEditorById(templateId)
    } finally {
      submitLoading.value = false
    }
  }

  async function confirmExitEditor() {
    if (!mailEditorRef.value?.hasUnsavedChanges()) return true
    try {
      await ElMessageBox.confirm(
        t('pages.config.mailTemplate.messages.confirmExitEditor'),
        t('pages.config.mailTemplate.messages.exitEditorTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
      return true
    } catch {
      return false
    }
  }

  async function onEditorBack() {
    if (!(await confirmExitEditor())) return
    mailEditorRef.value?.clearEditorState()
    transitionDirection.value = 'slide-right'
    activeTab.value = 'list'
    editingTemplate.value = null
    clearEditorQuery()
    clearEditorSession()
    void refreshData()
  }

  function onEditorSaved() {
    // 编辑器已保存，无需额外操作（列表将在返回时刷新）
  }

  async function openPreviewFromRow(row: MailTemplate) {
    const templateId = resolveTemplateId(row)
    if (!templateId) return

    await withPageLoading(async () => {
      const detail = await fetchMailTemplateGet(templateId)
      Object.assign(previewPayload, {
        sendType: detail.sendType || detail.type,
        subject: detail.subject || '',
        content: detail.content || ''
      })
      previewVisible.value = true
    })
  }

  async function handleSearch(params: MailTemplateSearchForm) {
    replaceSearchParams({
      name: params.name,
      sendType: params.sendType,
      templateType: params.templateType,
      enabled: params.enabled,
      remark: params.remark,
      createTimeStart: params.createTimeRange?.[0],
      createTimeEnd: params.createTimeRange?.[1],
      updateTimeStart: params.updateTimeRange?.[0],
      updateTimeEnd: params.updateTimeRange?.[1],
      page: 1
    })
    await withPageLoading(() => getTableData())
  }

  async function handleReset() {
    searchForm.value = {
      name: undefined,
      sendType: undefined,
      templateType: undefined,
      enabled: undefined,
      remark: undefined,
      createTimeRange: undefined,
      updateTimeRange: undefined
    }
    await withPageLoading(() => resetTableSearchParams())
  }

  async function handleEnabledChange(row: MailTemplate, val: boolean) {
    const templateId = resolveTemplateId(row)
    if (!templateId) return

    await withPageLoading(async () => {
      await fetchMailTemplateUpdateEnabled(templateId, val ? 1 : 0)
      row.enabled = val ? 1 : 0
      ElMessage.success(
        val
          ? t('pages.config.mailTemplate.messages.enabledSuccess')
          : t('pages.config.mailTemplate.messages.disabledSuccess')
      )
      // 启用互斥由后端保证（同一发件类型仅一条启用），刷新以同步同组其它行状态。
      if (val) await refreshTableData()
    })
  }

  async function handleDelete(row: MailTemplate) {
    const templateId = resolveTemplateId(row)
    if (!templateId) return

    await ElMessageBox.confirm(
      t('pages.config.mailTemplate.messages.confirmDelete', { name: row.name }),
      t('pages.config.mailTemplate.messages.deleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    await withPageLoading(async () => {
      await fetchMailTemplateDelete(templateId)
      ElMessage.success(t('pages.config.mailTemplate.messages.deleteSuccess'))
      await refreshTableData()
    })
  }

  function handleSelectionChange(selection: MailTemplate[]) {
    selectedRows.value = selection
  }

  async function handleBatchDelete() {
    if (selectedRows.value.length === 0) {
      ElMessage.warning(t('common.batchDeleteEmpty'))
      return
    }
    if (hasSelectedSystemTemplate.value) {
      ElMessage.warning(t('pages.config.mailTemplate.messages.systemTemplateDeleteForbidden'))
      return
    }
    await ElMessageBox.confirm(
      t('common.batchDeleteConfirm', { count: selectedRows.value.length }),
      t('common.batchDeleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await withPageLoading(async () => {
      await fetchMailTemplateDeleteBatch(selectedRows.value.map((row) => row.id))
      ElMessage.success(t('common.batchDeleteSuccess'))
      veloxTableRef.value?.elTableRef?.clearSelection()
      selectedRows.value = []
      await refreshTableData()
    })
  }

  async function loadMetadata(showLoading = true) {
    const load = async () => {
      metadata.value = await fetchMailTemplateMetadata()
    }

    if (showLoading) {
      await withPageLoading(load)
    } else {
      await load()
    }
  }

  onMounted(() => {
    void loadMetadata(false)
    void restoreEditorState()
  })
</script>

<style scoped lang="scss">
  .mail-template-form {
    max-width: 820px;
    margin: 0 auto;
  }

  .mail-template-dialog {
    :deep(.el-dialog__body) {
      padding-top: 12px;
    }

    :deep(.el-dialog__footer) {
      padding-top: 8px;
    }

    :deep(.el-form-item__label) {
      color: var(--el-text-color-regular);
    }
  }

  .mail-template-page {
    min-height: 0;
  }

  .mail-template-editor-view {
    flex: 1;
    min-height: 0;
    margin-top: 0;
  }

  /* 列表 ↔ 编辑器过渡动画 */
  .slide-left-enter-active,
  .slide-left-leave-active,
  .slide-right-enter-active,
  .slide-right-leave-active {
    transition: all 0.22s cubic-bezier(0.25, 0.1, 0.25, 1);
    will-change: transform, opacity;
  }

  .slide-left-enter-from {
    opacity: 0;
    transform: translate3d(24px, 0, 0);
  }

  .slide-left-leave-to {
    opacity: 0;
    transform: translate3d(-24px, 0, 0);
  }

  .slide-right-enter-from {
    opacity: 0;
    transform: translate3d(-24px, 0, 0);
  }

  .slide-right-leave-to {
    opacity: 0;
    transform: translate3d(24px, 0, 0);
  }

  @media (width <= 640px) {
    .mail-template-form {
      max-width: 100%;
    }

    .mail-template-dialog {
      :deep(.el-form-item) {
        display: flex;
        flex-direction: column;
        align-items: stretch;
        margin-bottom: 18px;
      }

      :deep(.el-form-item__label) {
        display: flex;
        justify-content: flex-start;
        width: 100% !important;
        height: auto !important;
        min-height: 0;
        padding-top: 0;
        padding-bottom: 8px;
        line-height: 1.4 !important;
        text-align: left;
        white-space: normal;
      }

      :deep(.el-form-item__content) {
        display: flex;
        width: 100%;
        min-width: 0;
        margin-left: 0 !important;
      }
    }
  }

  .mail-template-operation-buttons {
    :deep(> *:last-child) {
      margin-right: 0;
    }
  }
</style>
