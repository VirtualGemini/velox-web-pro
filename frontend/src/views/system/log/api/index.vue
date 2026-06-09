<template>
  <div class="velox-full-height">
    <ApiLogSearch
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
              v-if="hasAuth('system:log:api:delete')"
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
              v-if="hasAuth('system:log:api:clean')"
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
      title="pages.system.log.api.detail.title"
      :record="currentRecord"
      :fields="detailFields"
      :tabs="detailTabs"
      hero-title-prefix-prop="requestMethod"
      hero-title-prop="requestUri"
    />
  </div>
</template>

<script setup lang="ts">
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchApiLogDetail,
    fetchApiLogList,
    fetchCleanApiLogs,
    fetchDeleteApiLogs,
    type ApiLogQuery,
    type ApiLogRecord,
    type LogResult
  } from '@/api/log'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import ApiLogSearch from '../modules/api-log-search.vue'
  import LogDetailDrawer, {
    type DetailField,
    type DetailTab
  } from '../components/LogDetailDrawer.vue'
  import AccountCell from '../components/AccountCell.vue'
  import LocationCell from '../components/LocationCell.vue'
  import {
    formatBytes,
    formatCostTime,
    formatFallback,
    getHttpStatusTagType,
    getLogResultTagType,
    getResultLabelKey
  } from '../utils'

  defineOptions({ name: 'ApiLog' })

  const { hasAuth } = useAuth()
  const { t } = useI18n()

  const veloxTableRef = ref()
  const showSearchBar = ref(false)
  const selectedRows = ref<ApiLogRecord[]>([])
  const detailVisible = ref(false)
  const currentRecord = ref<ApiLogRecord>()
  const searchForm = ref({
    requestUri: undefined,
    requestMethod: undefined,
    httpStatus: undefined,
    businessCode: undefined,
    result: undefined,
    costTimeMin: undefined,
    costTimeMax: undefined,
    username: undefined,
    clientIp: undefined,
    traceId: undefined,
    apiTimeRange: undefined as [string, string] | undefined
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
      apiFn: fetchApiLogList,
      apiParams: { current: 1, size: 20 } as ApiLogQuery,
      columnsFactory: () => [
        { type: 'selection', width: 52 },
        { type: 'index', label: 'table.column.index', width: 72 },
        {
          prop: 'requestMethod',
          label: 'pages.system.log.api.columns.requestMethod',
          minWidth: 100,
          formatter: (row: ApiLogRecord) =>
            h(ElTag, { effect: 'plain' }, () => formatFallback(row.requestMethod))
        },
        {
          prop: 'requestUri',
          label: 'pages.system.log.api.columns.requestUri',
          minWidth: 320,
          showOverflowTooltip: true
        },
        {
          prop: 'httpStatus',
          label: 'pages.system.log.api.columns.httpStatus',
          minWidth: 110,
          formatter: (row: ApiLogRecord) =>
            h(ElTag, { type: getHttpStatusTagType(row.httpStatus) }, () =>
              formatFallback(row.httpStatus)
            )
        },
        {
          prop: 'result',
          label: 'pages.system.log.common.columns.result',
          minWidth: 100,
          formatter: (row: ApiLogRecord) =>
            h(ElTag, { type: getLogResultTagType(row.result) }, () =>
              t(getResultLabelKey(row.result))
            )
        },
        {
          prop: 'username',
          label: 'pages.system.log.common.columns.account',
          minWidth: 180,
          formatter: (row: ApiLogRecord) =>
            h(AccountCell, { username: row.username, accountId: row.accountId })
        },
        {
          prop: 'ipLocation',
          label: 'pages.system.log.common.columns.location',
          minWidth: 160,
          formatter: (row: ApiLogRecord) =>
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
          formatter: (row: ApiLogRecord) => formatFallback(row.clientIp)
        },
        {
          prop: 'costTimeMs',
          label: 'pages.system.log.common.columns.costTime',
          minWidth: 110,
          formatter: (row: ApiLogRecord) => formatCostTime(row.costTimeMs)
        },
        { prop: 'createTime', label: 'pages.system.log.common.columns.createTime', minWidth: 180 },
        {
          prop: 'operation',
          label: 'pages.system.log.common.columns.operation',
          fixed: 'right',
          width: 90,
          formatter: (row: ApiLogRecord) =>
            h(
              'div',
              [
                hasAuth('system:log:api:query')
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
    { name: 'caller', label: 'pages.system.log.api.sections.caller' },
    { name: 'request', label: 'pages.system.log.common.sections.request' },
    { name: 'response', label: 'pages.system.log.api.sections.response' },
    { name: 'exception', label: 'pages.system.log.api.sections.exception' },
    { name: 'environment', label: 'pages.system.log.common.sections.environment' },
    { name: 'node', label: 'pages.system.log.api.sections.node' },
    { name: 'audit', label: 'pages.system.log.common.sections.audit' }
  ]

  const detailFields = computed<DetailField[]>(() => [
    { label: 'pages.system.log.common.detail.logId', prop: 'id', section: 'identity', full: true },
    { label: 'pages.system.log.common.detail.accountId', prop: 'accountId', section: 'identity' },
    { label: 'pages.system.log.common.detail.username', prop: 'username', section: 'identity' },
    { label: 'pages.system.log.api.detail.callerApp', prop: 'callerApp', section: 'caller' },
    {
      label: 'pages.system.log.api.detail.requestMethod',
      prop: 'requestMethod',
      section: 'request'
    },
    {
      label: 'pages.system.log.api.detail.requestUrl',
      prop: 'requestUrl',
      section: 'request',
      full: true
    },
    {
      label: 'pages.system.log.api.detail.requestUri',
      prop: 'requestUri',
      section: 'request',
      full: true
    },
    {
      label: 'pages.system.log.api.detail.matchedPattern',
      prop: 'matchedPattern',
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
      label: 'pages.system.log.api.detail.requestHeaders',
      prop: 'requestHeaders',
      section: 'request',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.api.detail.requestQuery',
      prop: 'requestQuery',
      section: 'request',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.api.detail.requestBody',
      prop: 'requestBody',
      section: 'request',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.api.detail.requestSize',
      prop: 'requestSize',
      section: 'request',
      formatter: (record) => formatBytes(record?.requestSize as number | undefined)
    },
    { label: 'pages.system.log.api.detail.httpStatus', prop: 'httpStatus', section: 'response' },
    {
      label: 'pages.system.log.api.detail.businessCode',
      prop: 'businessCode',
      section: 'response'
    },
    {
      label: 'pages.system.log.common.detail.result',
      prop: 'result',
      section: 'response',
      formatter: (record) => t(getResultLabelKey(record?.result as LogResult | undefined))
    },
    {
      label: 'pages.system.log.api.detail.businessMessage',
      prop: 'businessMessage',
      section: 'response',
      full: true
    },
    {
      label: 'pages.system.log.api.detail.responseBody',
      prop: 'responseBody',
      section: 'response',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.api.detail.responseSize',
      prop: 'responseSize',
      section: 'response',
      formatter: (record) => formatBytes(record?.responseSize as number | undefined)
    },
    {
      label: 'pages.system.log.common.detail.costTime',
      prop: 'costTimeMs',
      section: 'response',
      formatter: (record) => formatCostTime(record?.costTimeMs as number | undefined)
    },
    { label: 'pages.system.log.common.detail.errorCode', prop: 'errorCode', section: 'exception' },
    {
      label: 'pages.system.log.common.detail.errorMessage',
      prop: 'errorMessage',
      section: 'exception',
      code: true,
      full: true
    },
    {
      label: 'pages.system.log.api.detail.exceptionStack',
      prop: 'exceptionStack',
      section: 'exception',
      code: true,
      full: true
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
    { label: 'pages.system.log.api.detail.serverIp', prop: 'serverIp', section: 'node' },
    { label: 'pages.system.log.api.detail.serverNode', prop: 'serverNode', section: 'node' },
    { label: 'pages.system.log.api.detail.requestTime', prop: 'requestTime', section: 'audit' },
    { label: 'pages.system.log.api.detail.responseTime', prop: 'responseTime', section: 'audit' },
    { label: 'pages.system.log.api.detail.apiTime', prop: 'apiTime', section: 'audit' },
    { label: 'pages.system.log.common.detail.createTime', prop: 'createTime', section: 'audit' },
    { label: 'pages.system.log.common.detail.traceId', prop: 'traceId', section: 'audit' }
  ])

  function handleSearch(params: ApiLogQuery) {
    replaceSearchParams(params)
    getData(undefined, { operationLogQuery: true })
  }

  function handleSelectionChange(selection: ApiLogRecord[]) {
    selectedRows.value = selection
  }

  async function showDetail(row: ApiLogRecord) {
    currentRecord.value = row
    detailVisible.value = true
    try {
      currentRecord.value = await fetchApiLogDetail(row.id)
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
        t('pages.system.log.api.messages.confirmDelete', { count: selectedRows.value.length }),
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
    await fetchDeleteApiLogs(selectedRows.value.map((item) => item.id))
    ElMessage.success(t('common.batchDeleteSuccess'))
    clearSelection()
    refreshData()
  }

  async function handleClean() {
    try {
      await ElMessageBox.confirm(
        t('pages.system.log.api.messages.confirmClean'),
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
    await fetchCleanApiLogs()
    ElMessage.success(t('pages.system.log.common.messages.cleanSuccess'))
    clearSelection()
    refreshData()
  }

  function clearSelection() {
    veloxTableRef.value?.elTableRef?.clearSelection()
    selectedRows.value = []
  }
</script>
