<template>
  <div class="velox-full-height">
    <LoginLogSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
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
              v-if="hasAuth('system:log:login:delete')"
              class="velox-batch-delete"
              type="danger"
              plain
              :disabled="selectedRows.length === 0"
              @click="handleDelete"
              v-ripple
            >
              {{ t('common.batchDelete') }}
            </ElButton>
            <ElButton
              v-if="hasAuth('system:log:login:clean')"
              type="warning"
              plain
              @click="handleClean"
              v-ripple
            >
              {{ t('pages.system.log.common.actions.clean') }}
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

    <LogDetailDrawer
      v-model:visible="detailVisible"
      title="pages.system.log.login.detail.title"
      :record="currentRecord"
      :fields="detailFields"
      :tabs="detailTabs"
      hero-title-prop="eventType"
    />
  </div>
</template>

<script setup lang="ts">
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchCleanLoginLogs,
    fetchDeleteLoginLogs,
    fetchLoginLogDetail,
    fetchLoginLogList,
    type LoginLogQuery,
    type LoginLogRecord,
    type LogResult
  } from '@/api/log'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import LoginLogSearch from '../modules/login-log-search.vue'
  import LogDetailDrawer, {
    type DetailField,
    type DetailTab
  } from '../components/LogDetailDrawer.vue'
  import AccountCell from '../components/AccountCell.vue'
  import LocationCell from '../components/LocationCell.vue'
  import { formatFallback, getLogResultTagType, getResultLabelKey } from '../utils'

  defineOptions({ name: 'LoginLog' })

  const { hasAuth } = useAuth()
  const { t, te } = useI18n()

  const veloxTableRef = ref()
  const showSearchBar = ref(false)
  const selectedRows = ref<LoginLogRecord[]>([])
  const detailVisible = ref(false)
  const currentRecord = ref<LoginLogRecord>()
  const searchForm = ref({
    username: undefined,
    eventType: undefined,
    loginMethod: undefined,
    mfaType: undefined,
    result: undefined,
    failureCode: undefined,
    clientIp: undefined,
    countryName: undefined,
    provinceName: undefined,
    cityName: undefined,
    traceId: undefined,
    eventTimeRange: undefined as [string, string] | undefined
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
      apiFn: fetchLoginLogList,
      apiParams: { current: 1, size: 20 } as LoginLogQuery,
      columnsFactory: () => [
        { type: 'selection', width: 52 },
        { type: 'index', label: 'table.column.index', width: 72 },
        {
          prop: 'username',
          label: 'pages.system.log.common.columns.account',
          minWidth: 180,
          formatter: (row: LoginLogRecord) =>
            h(AccountCell, { username: row.username, accountId: row.accountId })
        },
        {
          prop: 'eventType',
          label: 'pages.system.log.login.columns.eventType',
          minWidth: 130,
          formatter: (row: LoginLogRecord) =>
            formatEnumLabel('pages.system.log.login.eventTypes', row.eventType)
        },
        {
          prop: 'loginMethod',
          label: 'pages.system.log.login.columns.loginMethod',
          minWidth: 130,
          formatter: (row: LoginLogRecord) => formatLoginMethod(row.loginMethod)
        },
        {
          prop: 'result',
          label: 'pages.system.log.common.columns.result',
          minWidth: 100,
          formatter: (row: LoginLogRecord) =>
            h(ElTag, { type: getLogResultTagType(row.result) }, () =>
              t(getResultLabelKey(row.result))
            )
        },
        {
          prop: 'ipLocation',
          label: 'pages.system.log.common.columns.location',
          minWidth: 160,
          formatter: (row: LoginLogRecord) =>
            h(LocationCell, {
              ipLocation: row.ipLocation,
              countryName: row.countryName,
              provinceName: row.provinceName,
              cityName: row.cityName
            })
        },
        {
          prop: 'clientIp',
          label: 'pages.system.log.common.columns.clientIp',
          minWidth: 140,
          formatter: (row: LoginLogRecord) => formatFallback(row.clientIp)
        },
        {
          prop: 'isp',
          label: 'pages.system.log.common.columns.isp',
          minWidth: 120,
          formatter: (row: LoginLogRecord) => formatFallback(row.isp)
        },
        {
          prop: 'browser',
          label: 'pages.system.log.login.columns.browserOs',
          minWidth: 170,
          formatter: (row: LoginLogRecord) =>
            `${formatFallback(row.browser)} / ${formatFallback(row.os)}`
        },
        { prop: 'createTime', label: 'pages.system.log.common.columns.createTime', minWidth: 180 },
        {
          prop: 'operation',
          label: 'pages.system.log.common.columns.operation',
          fixed: 'right',
          width: 90,
          formatter: (row: LoginLogRecord) =>
            h(
              'div',
              [
                hasAuth('system:log:login:query')
                  ? h(VeloxButtonTable, {
                      type: 'view',
                      onClick: () => showDetail(row)
                    })
                  : null
              ].filter(Boolean)
            )
        }
      ]
    }
  })

  const detailTabs: DetailTab[] = [
    { name: 'identity', label: 'pages.system.log.common.sections.identity' },
    { name: 'auth', label: 'pages.system.log.login.sections.auth' },
    { name: 'session', label: 'pages.system.log.login.sections.session' },
    { name: 'environment', label: 'pages.system.log.common.sections.environment' },
    { name: 'audit', label: 'pages.system.log.common.sections.audit' }
  ]

  const detailFields = computed<DetailField[]>(() => [
    { label: 'pages.system.log.common.detail.logId', prop: 'id', section: 'identity', full: true },
    { label: 'pages.system.log.common.detail.accountId', prop: 'accountId', section: 'identity' },
    { label: 'pages.system.log.common.detail.username', prop: 'username', section: 'identity' },
    {
      label: 'pages.system.log.login.detail.eventType',
      prop: 'eventType',
      section: 'auth',
      formatter: (record) =>
        formatEnumLabel('pages.system.log.login.eventTypes', record?.eventType as string)
    },
    {
      label: 'pages.system.log.login.detail.loginMethod',
      prop: 'loginMethod',
      section: 'auth',
      formatter: (record) => formatLoginMethod(record?.loginMethod as string)
    },
    { label: 'pages.system.log.login.detail.mfaType', prop: 'mfaType', section: 'auth' },
    {
      label: 'pages.system.log.common.detail.result',
      prop: 'result',
      section: 'auth',
      formatter: (record) => t(getResultLabelKey(record?.result as LogResult | undefined))
    },
    {
      label: 'pages.system.log.login.detail.firstLogin',
      prop: 'firstLogin',
      section: 'auth',
      formatter: (record) => formatBoolean(record?.firstLogin as number | undefined)
    },
    {
      label: 'pages.system.log.login.detail.riskType',
      prop: 'riskType',
      section: 'auth',
      formatter: (record) =>
        formatEnumLabel('pages.system.log.login.riskTypes', record?.riskType as string)
    },
    {
      label: 'pages.system.log.login.detail.failureCode',
      prop: 'failureCode',
      section: 'auth'
    },
    {
      label: 'pages.system.log.login.detail.failureMessage',
      prop: 'failureMessage',
      section: 'auth',
      full: true
    },
    { label: 'pages.system.log.login.detail.sessionId', prop: 'sessionId', section: 'session' },
    { label: 'pages.system.log.login.detail.eventTime', prop: 'eventTime', section: 'session' },
    { label: 'pages.system.log.login.detail.logoutTime', prop: 'logoutTime', section: 'session' },
    { label: 'pages.system.log.common.detail.clientIp', prop: 'clientIp', section: 'environment' },
    {
      label: 'pages.system.log.common.detail.location',
      prop: 'ipLocation',
      section: 'environment'
    },
    { label: 'pages.system.log.common.detail.isp', prop: 'isp', section: 'environment' },
    {
      label: 'pages.system.log.common.detail.deviceType',
      prop: 'deviceType',
      section: 'environment'
    },
    { label: 'pages.system.log.login.detail.browser', prop: 'browser', section: 'environment' },
    { label: 'pages.system.log.login.detail.os', prop: 'os', section: 'environment' },
    {
      label: 'pages.system.log.common.detail.userAgent',
      prop: 'userAgent',
      section: 'environment',
      code: true,
      full: true
    },
    { label: 'pages.system.log.common.detail.createTime', prop: 'createTime', section: 'audit' },
    { label: 'pages.system.log.common.detail.traceId', prop: 'traceId', section: 'audit' }
  ])

  function handleSearch(params: LoginLogQuery) {
    replaceSearchParams(params)
    getData(undefined, { operationLogQuery: true })
  }

  function handleSelectionChange(selection: LoginLogRecord[]) {
    selectedRows.value = selection
  }

  async function showDetail(row: LoginLogRecord) {
    currentRecord.value = row
    detailVisible.value = true
    try {
      currentRecord.value = await fetchLoginLogDetail(row.id)
    } catch {
      detailVisible.value = false
      ElMessage.error(t('httpMsg.requestFailed'))
    }
  }

  async function handleDelete() {
    if (selectedRows.value.length === 0) {
      ElMessage.warning(t('common.batchDeleteEmpty'))
      return
    }
    try {
      await ElMessageBox.confirm(
        t('pages.system.log.login.messages.confirmDelete', { count: selectedRows.value.length }),
        t('common.batchDeleteTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
    } catch {
      return
    }
    await fetchDeleteLoginLogs(selectedRows.value.map((item) => item.id))
    ElMessage.success(t('common.batchDeleteSuccess'))
    clearSelection()
    refreshData()
  }

  async function handleClean() {
    try {
      await ElMessageBox.confirm(
        t('pages.system.log.login.messages.confirmClean'),
        t('pages.system.log.common.messages.cleanTitle'),
        {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
      )
    } catch {
      return
    }
    await fetchCleanLoginLogs()
    ElMessage.success(t('pages.system.log.common.messages.cleanSuccess'))
    clearSelection()
    refreshData()
  }

  function clearSelection() {
    veloxTableRef.value?.elTableRef?.clearSelection()
    selectedRows.value = []
  }

  function formatEnumLabel(prefix: string, value?: string) {
    if (!value) return '-'
    const key = `${prefix}.${value}`
    return te(key) ? t(key) : value
  }

  function formatLoginMethod(value?: string) {
    if (!value) return '-'
    const labelKeys: Record<string, string> = {
      password: 'pages.system.log.login.methods.password',
      email_code: 'pages.system.log.login.methods.emailCode',
      oauth: 'pages.system.log.login.methods.oauth'
    }
    const key = labelKeys[value]
    return key ? t(key) : value
  }

  function formatBoolean(value?: number) {
    if (value === null || value === undefined) return '-'
    return value === 1 ? t('common.status.yes') : t('common.status.no')
  }
</script>
