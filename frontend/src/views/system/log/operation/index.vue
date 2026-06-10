<template>
  <div class="velox-full-height">
    <OperationLogSearch
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
              v-if="hasAuth('system:log:operation:delete')"
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
              v-if="hasAuth('system:log:operation:clean')"
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
      title="pages.system.log.operation.detail.title"
      :record="currentRecord"
      :fields="detailFields"
      :tabs="detailTabs"
      hero-title-prop="actionName"
      :hero-title-formatter="formatActionNameFromRecord"
    />
  </div>
</template>

<script setup lang="ts">
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchCleanOperationLogs,
    fetchDeleteOperationLogs,
    fetchOperationLogDetail,
    fetchOperationLogList,
    type LogResult,
    type OperationLogQuery,
    type OperationLogRecord
  } from '@/api/log'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import OperationLogSearch from '../modules/operation-log-search.vue'
  import LogDetailDrawer, {
    type DetailField,
    type DetailTab
  } from '../components/LogDetailDrawer.vue'
  import AccountCell from '../components/AccountCell.vue'
  import LocationCell from '../components/LocationCell.vue'
  import { formatCostTime, formatFallback, getLogResultTagType, getResultLabelKey } from '../utils'

  defineOptions({ name: 'OperationLog' })

  const { hasAuth } = useAuth()
  const { t, te } = useI18n()

  const veloxTableRef = ref()
  const showSearchBar = ref(false)
  const selectedRows = ref<OperationLogRecord[]>([])
  const detailVisible = ref(false)
  const currentRecord = ref<OperationLogRecord>()
  const searchForm = ref({
    moduleName: undefined,
    actionName: undefined,
    operationType: undefined,
    username: undefined,
    result: undefined,
    clientIp: undefined,
    traceId: undefined,
    operationTimeRange: undefined as [string, string] | undefined
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
      apiFn: fetchOperationLogList,
      apiParams: { current: 1, size: 20 } as OperationLogQuery,
      columnsFactory: () => [
        { type: 'selection', width: 52 },
        { type: 'index', label: 'table.column.index', width: 72 },
        {
          prop: 'moduleName',
          label: 'pages.system.log.operation.columns.moduleName',
          minWidth: 140
        },
        {
          prop: 'actionName',
          label: 'pages.system.log.operation.columns.actionName',
          minWidth: 160,
          formatter: (row: OperationLogRecord) =>
            h(ElTag, { type: getActionTagType(row.actionName), effect: 'plain' }, () =>
              formatActionName(row.actionName)
            )
        },
        {
          prop: 'operationType',
          label: 'pages.system.log.operation.columns.operationType',
          minWidth: 130,
          formatter: (row: OperationLogRecord) =>
            formatEnumLabel('pages.system.log.operation.types', row.operationType)
        },
        {
          prop: 'username',
          label: 'pages.system.log.common.columns.account',
          minWidth: 180,
          formatter: (row: OperationLogRecord) =>
            h(AccountCell, { username: row.username, accountId: row.accountId })
        },
        {
          prop: 'result',
          label: 'pages.system.log.common.columns.result',
          minWidth: 100,
          formatter: (row: OperationLogRecord) =>
            h(ElTag, { type: getLogResultTagType(row.result) }, () =>
              t(getResultLabelKey(row.result))
            )
        },
        {
          prop: 'ipLocation',
          label: 'pages.system.log.common.columns.location',
          minWidth: 160,
          formatter: (row: OperationLogRecord) =>
            h(LocationCell, {
              ipLocation: row.ipLocation,
              countryName: row.countryName,
              provinceName: row.provinceName,
              cityName: row.cityName,
              isp: row.isp
            })
        },
        {
          prop: 'clientIp',
          label: 'pages.system.log.common.columns.clientIp',
          minWidth: 140,
          formatter: (row: OperationLogRecord) => formatFallback(row.clientIp)
        },
        {
          prop: 'costTimeMs',
          label: 'pages.system.log.common.columns.costTime',
          minWidth: 110,
          formatter: (row: OperationLogRecord) => formatCostTime(row.costTimeMs)
        },
        {
          prop: 'createTime',
          label: 'pages.system.log.common.columns.createTime',
          minWidth: 180
        },
        {
          prop: 'operation',
          label: 'pages.system.log.common.columns.operation',
          fixed: 'right',
          width: 90,
          formatter: (row: OperationLogRecord) =>
            h(
              'div',
              [
                hasAuth('system:log:operation:query')
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
    { name: 'operation', label: 'pages.system.log.operation.sections.operation' },
    { name: 'request', label: 'pages.system.log.common.sections.request' },
    { name: 'change', label: 'pages.system.log.operation.sections.change' },
    { name: 'result', label: 'pages.system.log.common.sections.result' },
    { name: 'environment', label: 'pages.system.log.common.sections.environment' },
    { name: 'audit', label: 'pages.system.log.common.sections.audit' }
  ]

  const detailFields = computed<DetailField[]>(() => [
    { label: 'pages.system.log.common.detail.logId', prop: 'id', section: 'identity', full: true },
    { label: 'pages.system.log.common.detail.accountId', prop: 'accountId', section: 'identity' },
    { label: 'pages.system.log.common.detail.username', prop: 'username', section: 'identity' },
    {
      label: 'pages.system.log.operation.detail.moduleName',
      prop: 'moduleName',
      section: 'operation'
    },
    {
      label: 'pages.system.log.operation.detail.actionName',
      prop: 'actionName',
      section: 'operation',
      tag: true,
      formatter: (record) => formatActionName(record?.actionName),
      tagType: (record) => getActionTagType(record?.actionName)
    },
    {
      label: 'pages.system.log.operation.detail.operationType',
      prop: 'operationType',
      section: 'operation',
      formatter: (record) =>
        formatEnumLabel('pages.system.log.operation.types', record?.operationType as string)
    },
    {
      label: 'pages.system.log.operation.detail.targetType',
      prop: 'targetType',
      section: 'operation'
    },
    { label: 'pages.system.log.operation.detail.targetId', prop: 'targetId', section: 'operation' },
    {
      label: 'pages.system.log.operation.detail.requestMethod',
      prop: 'requestMethod',
      section: 'request'
    },
    {
      label: 'pages.system.log.operation.detail.requestUri',
      prop: 'requestUri',
      section: 'request',
      full: true
    },
    {
      label: 'pages.system.log.common.detail.javaMethod',
      prop: 'javaMethod',
      section: 'request',
      full: true
    },
    {
      label: 'pages.system.log.operation.detail.requestParams',
      prop: 'requestParams',
      section: 'request',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.operation.detail.beforeData',
      prop: 'beforeData',
      section: 'change',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.operation.detail.afterData',
      prop: 'afterData',
      section: 'change',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.operation.detail.responseSummary',
      prop: 'responseSummary',
      section: 'result',
      code: true,
      full: true
    },
    { label: 'pages.system.log.common.detail.errorCode', prop: 'errorCode', section: 'result' },
    {
      label: 'pages.system.log.common.detail.errorMessage',
      prop: 'errorMessage',
      section: 'result',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.common.detail.costTime',
      prop: 'costTimeMs',
      section: 'result',
      formatter: (record) => formatCostTime(record?.costTimeMs as number | undefined)
    },
    {
      label: 'pages.system.log.common.detail.result',
      prop: 'result',
      section: 'result',
      formatter: (record) => t(getResultLabelKey(record?.result as LogResult | undefined))
    },
    { label: 'pages.system.log.common.detail.clientIp', prop: 'clientIp', section: 'environment' },
    {
      label: 'pages.system.log.common.detail.location',
      prop: 'ipLocation',
      section: 'environment'
    },
    {
      label: 'pages.system.log.common.detail.deviceType',
      prop: 'deviceType',
      section: 'environment'
    },
    { label: 'pages.system.log.common.detail.browser', prop: 'browser', section: 'environment' },
    { label: 'pages.system.log.common.detail.os', prop: 'os', section: 'environment' },
    {
      label: 'pages.system.log.common.detail.userAgent',
      prop: 'userAgent',
      section: 'environment',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.operation.detail.operationTime',
      prop: 'operationTime',
      section: 'audit'
    },
    { label: 'pages.system.log.common.detail.createTime', prop: 'createTime', section: 'audit' },
    { label: 'pages.system.log.common.detail.traceId', prop: 'traceId', section: 'audit' }
  ])

  function handleSearch(params: OperationLogQuery) {
    replaceSearchParams(params)
    getData(undefined, { operationLogQuery: true })
  }

  type ActionTagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

  function formatActionName(value: unknown) {
    const text = value === null || value === undefined || value === '' ? '-' : String(value)
    return text === '-' ? text : text.toUpperCase()
  }

  function formatActionNameFromRecord(record?: Record<string, unknown>) {
    return formatActionName(record?.actionName)
  }

  function getActionTagType(value: unknown): ActionTagType {
    const action = String(value || '').toUpperCase()
    if (action.includes('DELETE')) return 'danger'
    if (action.includes('CLEAN') || action.includes('FORCE')) return 'warning'
    if (action.includes('CREATE')) return 'success'
    if (action.includes('UPDATE')) return 'primary'
    return 'info'
  }

  function handleSelectionChange(selection: OperationLogRecord[]) {
    selectedRows.value = selection
  }

  async function showDetail(row: OperationLogRecord) {
    currentRecord.value = row
    detailVisible.value = true
    try {
      currentRecord.value = await fetchOperationLogDetail(row.id)
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
        t('pages.system.log.operation.messages.confirmDelete', {
          count: selectedRows.value.length
        }),
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
    await fetchDeleteOperationLogs(selectedRows.value.map((item) => item.id))
    ElMessage.success(t('common.batchDeleteSuccess'))
    clearSelection()
    refreshData()
  }

  async function handleClean() {
    try {
      await ElMessageBox.confirm(
        t('pages.system.log.operation.messages.confirmClean'),
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
    await fetchCleanOperationLogs()
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
</script>
