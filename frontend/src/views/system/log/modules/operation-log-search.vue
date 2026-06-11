<template>
  <VeloxSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    @reset="handleReset"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  import type { OperationLogQuery } from '@/api/log'
  import { LOG_RESULT_OPTIONS, OPERATION_TYPE_OPTIONS } from '../constants'

  type OperationLogSearchForm = {
    moduleName?: string
    operationType?: string
    username?: string
    result?: 0 | 1
    clientIp?: string
    countryName?: string
    provinceName?: string
    cityName?: string
    operationTimeRange?: [string, string]
    recordTimeRange?: [string, string]
  }

  interface Props {
    modelValue: OperationLogSearchForm
  }

  interface Emits {
    (e: 'update:modelValue', value: OperationLogSearchForm): void
    (e: 'search', params: OperationLogQuery): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const formItems = computed(() => [
    {
      label: 'pages.system.log.operation.search.moduleName',
      key: 'moduleName',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.operation.search.operationType',
      key: 'operationType',
      type: 'select',
      props: { options: OPERATION_TYPE_OPTIONS, clearable: true }
    },
    {
      label: 'pages.system.log.common.search.username',
      key: 'username',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.common.search.result',
      key: 'result',
      type: 'select',
      props: { options: LOG_RESULT_OPTIONS, clearable: true }
    },
    {
      label: 'pages.system.log.common.search.clientIp',
      key: 'clientIp',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.common.search.countryName',
      key: 'countryName',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.common.search.provinceName',
      key: 'provinceName',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.common.search.cityName',
      key: 'cityName',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.operation.search.operationTimeRange',
      key: 'operationTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.log.common.search.placeholders.startTime',
        endPlaceholder: 'pages.system.log.common.search.placeholders.endTime',
        rangeSeparator: 'pages.system.log.common.search.rangeSeparator'
      }
    },
    {
      label: 'pages.system.log.common.search.recordTimeRange',
      key: 'recordTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.log.common.search.placeholders.startTime',
        endPlaceholder: 'pages.system.log.common.search.placeholders.endTime',
        rangeSeparator: 'pages.system.log.common.search.rangeSeparator'
      }
    }
  ])

  function handleReset() {
    emit('reset')
  }

  async function handleSearch(params: OperationLogSearchForm) {
    await searchBarRef.value.validate()
    emit('search', {
      moduleName: params.moduleName,
      operationType: params.operationType,
      username: params.username,
      result: params.result,
      clientIp: params.clientIp,
      countryName: params.countryName,
      provinceName: params.provinceName,
      cityName: params.cityName,
      operationTimeStart: params.operationTimeRange?.[0],
      operationTimeEnd: params.operationTimeRange?.[1],
      createTimeStart: params.recordTimeRange?.[0],
      createTimeEnd: params.recordTimeRange?.[1]
    })
  }
</script>
