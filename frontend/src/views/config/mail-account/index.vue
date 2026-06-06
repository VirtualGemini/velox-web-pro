<template>
  <div class="velox-full-height">
    <VeloxSearchBar
      v-show="showSearchBar"
      v-model="searchForm"
      :items="searchItems"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard class="velox-table-card" :style="{ 'margin-top': showSearchBar ? '12px' : '0' }">
      <VeloxTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton
              v-if="canAccess('system:mail-account:create')"
              @click="openCreateDialog"
              v-ripple
            >
              {{ t('pages.config.mailAccount.actions.add') }}
            </ElButton>
            <ElButton
              v-if="canAccess('system:mail-account:group')"
              @click="groupDialogVisible = true"
              v-ripple
            >
              {{ t('pages.config.mailAccount.actions.group') }}
            </ElButton>
            <ElButton
              v-if="canAccess('system:mail-account:channel')"
              @click="channelDialogVisible = true"
              v-ripple
            >
              {{ t('pages.config.mailAccount.actions.channel') }}
            </ElButton>
            <ElButton
              v-if="canAccess('system:mail-account:delete')"
              class="velox-batch-delete"
              type="danger"
              plain
              :disabled="selectedRows.length === 0"
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

    <ElDialog
      v-model="dialogVisible"
      :title="
        dialogMode === 'create'
          ? t('pages.config.mailAccount.form.titles.create')
          : t('pages.config.mailAccount.form.titles.edit')
      "
      :width="dialogWidth"
      align-center
      class="mail-account-dialog"
      :close-on-click-modal="false"
      @close="handleDialogClose"
      @closed="handleDialogClosed"
    >
      <ElForm
        ref="formRef"
        :model="formModel"
        :rules="formRules"
        :label-width="formLabelWidth"
        label-position="right"
        class="mail-account-form"
      >
        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.mailAccount.form.fields.name')" prop="name">
              <ElInput
                v-model="formModel.name"
                maxlength="100"
                :placeholder="t('pages.config.mailAccount.form.placeholders.name')"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem prop="groupId">
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.group')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.group')"
                />
              </template>
              <ElSelect
                v-model="formModel.groupId"
                :placeholder="t('pages.config.mailAccount.form.placeholders.group')"
              >
                <ElOption
                  v-for="option in groupOptions"
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
            <ElFormItem prop="channelId">
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.channel')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.channel')"
                />
              </template>
              <ElSelect
                v-model="formModel.channelId"
                :placeholder="t('pages.config.mailAccount.form.placeholders.channel')"
              >
                <ElOption
                  v-for="option in channelOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.mailAccount.form.fields.username')" prop="username">
              <ElInput
                v-model="formModel.username"
                maxlength="200"
                :placeholder="t('pages.config.mailAccount.form.placeholders.username')"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.mailAccount.form.fields.password')" prop="password">
              <ElInput
                v-model="formModel.password"
                type="password"
                show-password
                autocomplete="new-password"
                :placeholder="
                  dialogMode === 'create'
                    ? t('pages.config.mailAccount.form.placeholders.passwordCreate')
                    : t('pages.config.mailAccount.form.placeholders.passwordEdit')
                "
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.mailAccount.form.fields.fromAddress')">
              <ElInput
                v-model="formModel.fromAddress"
                maxlength="200"
                :placeholder="t('pages.config.mailAccount.form.placeholders.fromAddress')"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.mailAccount.form.fields.fromName')">
              <ElInput
                v-model="formModel.fromName"
                maxlength="100"
                :placeholder="t('pages.config.mailAccount.form.placeholders.fromName')"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem>
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.host')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.host')"
                />
              </template>
              <ElInput
                v-model="formModel.host"
                maxlength="200"
                :placeholder="t('pages.config.mailAccount.form.placeholders.host')"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem>
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.port')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.port')"
                />
              </template>
              <ElInputNumber
                v-model="formModel.port"
                class="w-full"
                :min="1"
                :max="65535"
                controls-position="right"
                :placeholder="t('pages.config.mailAccount.form.placeholders.port')"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem>
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.ssl')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.ssl')"
                />
              </template>
              <ElSelect
                v-model="formModel.sslEnabled"
                clearable
                :placeholder="t('pages.config.mailAccount.form.options.triState.auto')"
              >
                <ElOption
                  :label="t('pages.config.mailAccount.form.options.triState.on')"
                  :value="1"
                />
                <ElOption
                  :label="t('pages.config.mailAccount.form.options.triState.off')"
                  :value="0"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem>
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.starttls')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.starttls')"
                />
              </template>
              <ElSelect
                v-model="formModel.starttls"
                clearable
                :placeholder="t('pages.config.mailAccount.form.options.triState.auto')"
              >
                <ElOption
                  :label="t('pages.config.mailAccount.form.options.triState.on')"
                  :value="1"
                />
                <ElOption
                  :label="t('pages.config.mailAccount.form.options.triState.off')"
                  :value="0"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem prop="weight">
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.weight')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.weight')"
                />
              </template>
              <ElInputNumber
                v-model="formModel.weight"
                class="w-full"
                :min="1"
                :max="100000"
                controls-position="right"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem prop="failThreshold">
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.failThreshold')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.failThreshold')"
                />
              </template>
              <ElInputNumber
                v-model="formModel.failThreshold"
                class="w-full"
                :min="1"
                :max="1000"
                controls-position="right"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem prop="retryInterval">
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.retryInterval')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.retryInterval')"
                />
              </template>
              <ElInputNumber
                v-model="formModel.retryInterval"
                class="w-full"
                :min="0"
                :max="86400"
                controls-position="right"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElRow :gutter="16">
          <ElCol :span="formHalfSpan">
            <ElFormItem prop="maxUnavailable">
              <template #label>
                <LabelTooltip
                  :label="t('pages.config.mailAccount.form.fields.maxUnavailable')"
                  :tooltip="t('pages.config.mailAccount.form.tooltips.maxUnavailable')"
                />
              </template>
              <ElInputNumber
                v-model="formModel.maxUnavailable"
                class="w-full"
                :min="1"
                :max="1000"
                controls-position="right"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="formHalfSpan">
            <ElFormItem :label="t('pages.config.mailAccount.form.fields.enabled')">
              <ElSwitch v-model="formModel.enabled" />
            </ElFormItem>
          </ElCol>
        </ElRow>

        <ElFormItem :label="t('pages.config.mailAccount.form.fields.remark')">
          <ElInput
            v-model="formModel.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            :placeholder="t('pages.config.mailAccount.form.placeholders.remark')"
          />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <ElButton @click="handleDialogClose">{{ t('common.cancel') }}</ElButton>
        <ElButton type="primary" @click="handleSubmit">
          {{ t('table.form.submit') }}
        </ElButton>
      </template>
    </ElDialog>

    <MailGroupDialog v-model:visible="groupDialogVisible" @changed="handleGroupChanged" />
    <MailChannelDialog v-model:visible="channelDialogVisible" @success="loadOptions" />
  </div>
</template>

<script setup lang="ts">
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
  import { useRoute } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { useWindowSize } from '@vueuse/core'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import MailGroupDialog from './components/MailGroupDialog.vue'
  import MailChannelDialog from './components/MailChannelDialog.vue'
  import {
    type MailAccount,
    type MailAccountSaveCommand,
    fetchMailAccountCreate,
    fetchMailAccountDelete,
    fetchMailAccountDeleteBatch,
    fetchMailAccountGet,
    fetchMailAccountPage,
    fetchMailAccountRecover,
    fetchMailAccountTest,
    fetchMailAccountUpdate,
    fetchMailAccountUpdateEnabled,
    fetchMailChannelList,
    fetchMailGroupList
  } from '@/api/mail'

  defineOptions({ name: 'MailAccount' })

  const HEALTH_STATUS = {
    NORMAL: 0,
    UNAVAILABLE: 1,
    DEAD: 2
  } as const

  const DEFAULT_WEIGHT = 100
  const DEFAULT_FAIL_THRESHOLD = 3
  const DEFAULT_RETRY_INTERVAL = 300
  const DEFAULT_MAX_UNAVAILABLE = 5

  const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/

  type DialogMode = 'create' | 'edit'

  interface MailAccountFormModel {
    name: string
    groupId: string
    channelId: string
    username: string
    password: string
    fromAddress: string
    fromName: string
    host: string
    port: number | undefined
    sslEnabled: number | undefined
    starttls: number | undefined
    weight: number
    failThreshold: number
    retryInterval: number
    maxUnavailable: number
    enabled: boolean
    remark: string
  }

  type MailAccountSearchForm = {
    name?: string
    groupId?: string
    channelId?: string
    healthStatus?: number
    enabled?: number
    createTimeRange?: [string, string]
    updateTimeRange?: [string, string]
  }

  interface SelectOption {
    label: string
    value: string
  }

  const { hasAuth } = useAuth()
  const route = useRoute()
  const { t } = useI18n()
  const { width } = useWindowSize()

  const isMobile = computed(() => width.value <= 640)
  const dialogWidth = computed(() => (isMobile.value ? 'calc(100vw - 24px)' : '920px'))
  const formHalfSpan = computed(() => (isMobile.value ? 24 : 12))
  const formLabelWidth = computed(() => (isMobile.value ? '140px' : 'auto'))

  const groupOptions = ref<SelectOption[]>([])
  const channelOptions = ref<SelectOption[]>([])

  const searchForm = ref<MailAccountSearchForm>({
    name: undefined,
    groupId: undefined,
    channelId: undefined,
    healthStatus: undefined,
    enabled: undefined,
    createTimeRange: undefined,
    updateTimeRange: undefined
  })
  const showSearchBar = ref(false)

  const searchItems = computed(() => [
    {
      label: 'pages.config.mailAccount.search.name',
      key: 'name',
      type: 'input',
      props: { clearable: true }
    },
    {
      label: 'pages.config.mailAccount.search.group',
      key: 'groupId',
      type: 'select',
      props: { clearable: true, options: groupOptions.value }
    },
    {
      label: 'pages.config.mailAccount.search.channel',
      key: 'channelId',
      type: 'select',
      props: { clearable: true, options: channelOptions.value }
    },
    {
      label: 'pages.config.mailAccount.search.healthStatus',
      key: 'healthStatus',
      type: 'select',
      props: {
        clearable: true,
        options: [
          { label: 'pages.config.mailAccount.health.normal', value: HEALTH_STATUS.NORMAL },
          {
            label: 'pages.config.mailAccount.health.unavailable',
            value: HEALTH_STATUS.UNAVAILABLE
          },
          { label: 'pages.config.mailAccount.health.dead', value: HEALTH_STATUS.DEAD }
        ]
      }
    },
    {
      label: 'pages.config.mailAccount.search.enabled',
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
      label: 'pages.config.mailAccount.search.createTimeRange',
      key: 'createTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.config.mailAccount.search.placeholders.startTime',
        endPlaceholder: 'pages.config.mailAccount.search.placeholders.endTime',
        rangeSeparator: 'pages.config.mailAccount.search.rangeSeparator'
      }
    },
    {
      label: 'pages.config.mailAccount.search.updateTimeRange',
      key: 'updateTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.config.mailAccount.search.placeholders.startTime',
        endPlaceholder: 'pages.config.mailAccount.search.placeholders.endTime',
        rangeSeparator: 'pages.config.mailAccount.search.rangeSeparator'
      }
    }
  ])

  const dialogVisible = ref(false)
  const dialogMode = ref<DialogMode>('create')
  const submitLoading = ref(false)
  const currentEditId = ref('')
  const veloxTableRef = ref()
  const selectedRows = ref<MailAccount[]>([])
  const formRef = ref<FormInstance>()
  const formModel = reactive(createDefaultForm())

  const groupDialogVisible = ref(false)
  const channelDialogVisible = ref(false)

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

  const hasMailAccountAuthBinding = computed(() =>
    routeAuthMarks.value.some((auth) => auth.startsWith('system:mail-account:'))
  )

  const canAccess = (auth: string) => {
    return hasMailAccountAuthBinding.value ? hasAuth(auth) : true
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

  function validatePassword(_rule: unknown, value: string, callback: (error?: Error) => void) {
    if (dialogMode.value === 'create' && !value?.trim()) {
      callback(new Error(t('pages.config.mailAccount.messages.enterPassword')))
      return
    }
    callback()
  }

  const formRules = reactive<FormRules<MailAccountFormModel>>({
    name: [
      { required: true, message: t('pages.config.mailAccount.messages.enterName'), trigger: 'blur' }
    ],
    groupId: [
      {
        required: true,
        message: t('pages.config.mailAccount.messages.selectGroup'),
        trigger: 'change'
      }
    ],
    channelId: [
      {
        required: true,
        message: t('pages.config.mailAccount.messages.selectChannel'),
        trigger: 'change'
      }
    ],
    username: [
      {
        required: true,
        message: t('pages.config.mailAccount.messages.enterUsername'),
        trigger: 'blur'
      }
    ],
    password: [{ validator: validatePassword, trigger: 'blur' }]
  })

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchMailAccountPage,
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
          label: 'pages.config.mailAccount.columns.name',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'username',
          label: 'pages.config.mailAccount.columns.username',
          minWidth: 190,
          showOverflowTooltip: true
        },
        {
          prop: 'groupName',
          label: 'pages.config.mailAccount.columns.group',
          minWidth: 120,
          showOverflowTooltip: true,
          formatter: (row: MailAccount) => row.groupName || '-'
        },
        {
          prop: 'channelName',
          label: 'pages.config.mailAccount.columns.channel',
          minWidth: 130,
          formatter: (row: MailAccount) => renderChannel(row)
        },
        {
          prop: 'weight',
          label: 'pages.config.mailAccount.columns.weight',
          minWidth: 90,
          align: 'center'
        },
        {
          prop: 'healthStatus',
          label: 'pages.config.mailAccount.columns.healthStatus',
          minWidth: 120,
          align: 'center',
          formatter: (row: MailAccount) => renderHealth(row)
        },
        {
          prop: 'usageCount',
          label: 'pages.config.mailAccount.columns.usageCount',
          minWidth: 100,
          align: 'center',
          formatter: (row: MailAccount) => row.usageCount ?? 0
        },
        {
          prop: 'enabled',
          label: 'pages.config.mailAccount.columns.enabled',
          minWidth: 100,
          formatter: (row: MailAccount) =>
            h(ElSwitch, {
              modelValue: row.enabled === 1,
              disabled: !canAccess('system:mail-account:update'),
              'onUpdate:modelValue': (val: string | number | boolean) =>
                handleEnabledChange(row, !!val)
            })
        },
        {
          prop: 'createTime',
          label: 'pages.config.mailAccount.columns.createTime',
          minWidth: 180
        },
        {
          prop: 'updateTime',
          label: 'pages.config.mailAccount.columns.updateTime',
          minWidth: 180
        },
        {
          prop: 'operation',
          label: 'pages.config.mailAccount.columns.operation',
          width: 168,
          fixed: 'right',
          align: 'left',
          formatter: (row: MailAccount) => renderOperationButtons(row)
        }
      ]
    }
  })

  function createDefaultForm(): MailAccountFormModel {
    return {
      name: '',
      groupId: '',
      channelId: '',
      username: '',
      password: '',
      fromAddress: '',
      fromName: '',
      host: '',
      port: undefined,
      sslEnabled: undefined,
      starttls: undefined,
      weight: DEFAULT_WEIGHT,
      failThreshold: DEFAULT_FAIL_THRESHOLD,
      retryInterval: DEFAULT_RETRY_INTERVAL,
      maxUnavailable: DEFAULT_MAX_UNAVAILABLE,
      enabled: true,
      remark: ''
    }
  }

  function renderChannel(row: MailAccount) {
    const showProtocol = !!row.protocol && row.protocol !== row.channelName
    return h('div', { class: 'flex items-center gap-1' }, [
      h('span', row.channelName || '-'),
      showProtocol ? h(ElTag, { size: 'small', type: 'info' }, () => row.protocol) : null
    ])
  }

  function renderHealth(row: MailAccount) {
    const presets: Record<number, { type: 'success' | 'warning' | 'danger'; key: string }> = {
      [HEALTH_STATUS.NORMAL]: { type: 'success', key: 'normal' },
      [HEALTH_STATUS.UNAVAILABLE]: { type: 'warning', key: 'unavailable' },
      [HEALTH_STATUS.DEAD]: { type: 'danger', key: 'dead' }
    }
    const preset = presets[row.healthStatus] ?? presets[HEALTH_STATUS.NORMAL]
    return h(ElTag, { type: preset.type }, () => t(`pages.config.mailAccount.health.${preset.key}`))
  }

  function renderOperationButtons(row: MailAccount) {
    const moreItems: ReturnType<typeof h>[] = []

    if (canAccess('system:mail-account:query')) {
      moreItems.push(
        h(ElDropdownItem, { onClick: () => handleTest(row) }, () =>
          t('pages.config.mailAccount.actions.test')
        )
      )
    }

    if (canAccess('system:mail-account:update') && row.healthStatus === HEALTH_STATUS.DEAD) {
      moreItems.push(
        h(ElDropdownItem, { onClick: () => handleRecover(row) }, () =>
          t('pages.config.mailAccount.actions.recover')
        )
      )
    }

    return h(
      'div',
      { class: 'mail-account-operation-buttons flex items-center' },
      [
        canAccess('system:mail-account:update')
          ? h(VeloxButtonTable, { type: 'edit', onClick: () => openEditDialog(row) })
          : null,
        canAccess('system:mail-account:delete')
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

  function resolveAccountId(row?: Pick<MailAccount, 'id'>) {
    const accountId = row?.id?.trim()
    if (!accountId) {
      ElMessage.error(t('pages.config.mailAccount.messages.missingId'))
      return null
    }
    return accountId
  }

  function fillForm(detail?: MailAccount) {
    Object.assign(formModel, createDefaultForm())
    if (!detail) return

    Object.assign(formModel, {
      name: detail.name || '',
      groupId: detail.groupId || '',
      channelId: detail.channelId || '',
      username: detail.username || '',
      password: '',
      fromAddress: detail.fromAddress || '',
      fromName: detail.fromName || '',
      host: detail.host || '',
      port: detail.port ?? undefined,
      sslEnabled: detail.sslEnabled ?? undefined,
      starttls: detail.starttls ?? undefined,
      weight: detail.weight ?? DEFAULT_WEIGHT,
      failThreshold: detail.failThreshold ?? DEFAULT_FAIL_THRESHOLD,
      retryInterval: detail.retryInterval ?? DEFAULT_RETRY_INTERVAL,
      maxUnavailable: detail.maxUnavailable ?? DEFAULT_MAX_UNAVAILABLE,
      enabled: detail.enabled === 1,
      remark: detail.remark || ''
    })
  }

  function openCreateDialog() {
    dialogMode.value = 'create'
    currentEditId.value = ''
    fillForm()
    if (groupOptions.value.length > 0) {
      formModel.groupId = groupOptions.value[0].value
    }
    if (channelOptions.value.length > 0) {
      formModel.channelId = channelOptions.value[0].value
    }
    dialogVisible.value = true
  }

  async function openEditDialog(row: MailAccount) {
    const accountId = resolveAccountId(row)
    if (!accountId) return

    const detail = await fetchMailAccountGet(accountId)
    dialogMode.value = 'edit'
    currentEditId.value = accountId
    fillForm(detail)
    dialogVisible.value = true
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

  function buildPayload(): MailAccountSaveCommand {
    return {
      name: formModel.name.trim(),
      groupId: formModel.groupId,
      channelId: formModel.channelId,
      username: formModel.username.trim(),
      password: formModel.password.trim() || undefined,
      fromAddress: formModel.fromAddress.trim() || undefined,
      fromName: formModel.fromName.trim() || undefined,
      host: formModel.host.trim() || undefined,
      port: formModel.port ?? undefined,
      sslEnabled: formModel.sslEnabled ?? undefined,
      starttls: formModel.starttls ?? undefined,
      weight: formModel.weight,
      failThreshold: formModel.failThreshold,
      retryInterval: formModel.retryInterval,
      maxUnavailable: formModel.maxUnavailable,
      enabled: formModel.enabled ? 1 : 0,
      remark: formModel.remark.trim() || undefined
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
        await fetchMailAccountCreate(payload)
        ElMessage.success(t('pages.config.mailAccount.messages.createSuccess'))
      } else {
        payload.id = currentEditId.value
        await fetchMailAccountUpdate(payload)
        ElMessage.success(t('pages.config.mailAccount.messages.updateSuccess'))
      }
      handleDialogClose()
      refreshData()
    } finally {
      submitLoading.value = false
    }
  }

  function handleSearch(params: MailAccountSearchForm) {
    replaceSearchParams({
      name: params.name,
      groupId: params.groupId,
      channelId: params.channelId,
      healthStatus: params.healthStatus,
      enabled: params.enabled,
      createTimeStart: params.createTimeRange?.[0],
      createTimeEnd: params.createTimeRange?.[1],
      updateTimeStart: params.updateTimeRange?.[0],
      updateTimeEnd: params.updateTimeRange?.[1],
      page: 1
    })
    getData()
  }

  function handleReset() {
    searchForm.value = {
      name: undefined,
      groupId: undefined,
      channelId: undefined,
      healthStatus: undefined,
      enabled: undefined,
      createTimeRange: undefined,
      updateTimeRange: undefined
    }
    resetSearchParams()
    getData()
  }

  async function loadOptions() {
    const [groups, channels] = await Promise.all([fetchMailGroupList(), fetchMailChannelList()])
    groupOptions.value = (groups || []).map((item) => ({ label: item.name, value: item.id }))
    channelOptions.value = (channels || []).map((item) => ({
      label: `${item.name} (${item.protocol})`,
      value: item.id
    }))
  }

  function handleGroupChanged() {
    loadOptions()
    refreshData()
  }

  onMounted(async () => {
    await loadOptions()
  })

  async function handleEnabledChange(row: MailAccount, val: boolean) {
    const accountId = resolveAccountId(row)
    if (!accountId) return

    await fetchMailAccountUpdateEnabled(accountId, val ? 1 : 0)
    row.enabled = val ? 1 : 0
    ElMessage.success(
      val
        ? t('pages.config.mailAccount.messages.enabledSuccess')
        : t('pages.config.mailAccount.messages.disabledSuccess')
    )
  }

  async function handleTest(row: MailAccount) {
    const accountId = resolveAccountId(row)
    if (!accountId) return

    let toEmail = ''
    try {
      const result = await ElMessageBox.prompt(
        t('pages.config.mailAccount.messages.testPromptPlaceholder'),
        t('pages.config.mailAccount.messages.testPromptTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          inputPattern: EMAIL_PATTERN,
          inputErrorMessage: t('pages.config.mailAccount.messages.testEmailInvalid'),
          inputValue: row.fromAddress || row.username || ''
        }
      )
      toEmail = result.value.trim()
    } catch {
      return
    }

    const sending = ElMessage({
      message: t('pages.config.mailAccount.messages.testSending'),
      type: 'info',
      duration: 0
    })
    try {
      await fetchMailAccountTest(accountId, toEmail)
      ElMessage.success(t('pages.config.mailAccount.messages.testSuccess'))
    } catch {
      // 发送失败的错误信息由请求拦截器统一提示。
    } finally {
      sending.close()
    }
  }

  async function handleRecover(row: MailAccount) {
    const accountId = resolveAccountId(row)
    if (!accountId) return

    await ElMessageBox.confirm(
      t('pages.config.mailAccount.messages.confirmRecover', { name: row.name }),
      t('pages.config.mailAccount.messages.recoverTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    await fetchMailAccountRecover(accountId)
    ElMessage.success(t('pages.config.mailAccount.messages.recoverSuccess'))
    refreshData()
  }

  async function handleDelete(row: MailAccount) {
    const accountId = resolveAccountId(row)
    if (!accountId) return

    await ElMessageBox.confirm(
      t('pages.config.mailAccount.messages.confirmDelete', { name: row.name }),
      t('pages.config.mailAccount.messages.deleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    await fetchMailAccountDelete(accountId)
    ElMessage.success(t('pages.config.mailAccount.messages.deleteSuccess'))
    refreshData()
  }

  function handleSelectionChange(selection: MailAccount[]) {
    selectedRows.value = selection
  }

  async function handleBatchDelete() {
    if (selectedRows.value.length === 0) {
      ElMessage.warning(t('common.batchDeleteEmpty'))
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
    await fetchMailAccountDeleteBatch(selectedRows.value.map((row) => row.id))
    ElMessage.success(t('common.batchDeleteSuccess'))
    veloxTableRef.value?.elTableRef?.clearSelection()
    selectedRows.value = []
    refreshData()
  }
</script>

<style scoped lang="scss">
  .mail-account-form {
    max-width: 820px;
    margin: 0 auto;
  }

  .mail-account-dialog {
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

  @media (width <= 640px) {
    .mail-account-form {
      max-width: 100%;
    }

    .mail-account-dialog {
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

      :deep(.el-form-item__label-wrap) {
        display: block;
        width: 100% !important;
        margin: 0;
      }

      :deep(.el-form-item__label > .flex) {
        display: inline-flex;
        flex-wrap: wrap;
        gap: 4px;
        align-items: center;
      }

      :deep(.el-form-item__content) {
        display: flex;
        width: 100%;
        min-width: 0;
        margin-left: 0 !important;
      }
    }
  }

  .mail-account-operation-buttons {
    :deep(> *:last-child) {
      margin-right: 0;
    }
  }
</style>
