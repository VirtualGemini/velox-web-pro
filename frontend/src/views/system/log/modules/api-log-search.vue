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
  import type { ApiLogQuery } from '@/api/log'
  import { HTTP_METHOD_OPTIONS, LOG_RESULT_OPTIONS } from '../constants'

  type ApiLogSearchForm = {
    requestUri?: string
    requestMethod?: string
    httpStatus?: number
    businessCode?: string
    result?: 0 | 1
    costTimeMin?: number
    costTimeMax?: number
    username?: string
    clientIp?: string
    traceId?: string
    apiTimeRange?: [string, string]
  }

  interface Props {
    modelValue: ApiLogSearchForm
  }

  interface Emits {
    (e: 'update:modelValue', value: ApiLogSearchForm): void
    (e: 'search', params: ApiLogQuery): void
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
      label: 'pages.system.log.api.search.requestUri',
      key: 'requestUri',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.api.search.requestMethod',
      key: 'requestMethod',
      type: 'select',
      props: { options: HTTP_METHOD_OPTIONS, clearable: true }
    },
    {
      label: 'pages.system.log.api.search.httpStatus',
      key: 'httpStatus',
      type: 'number',
      props: { min: 100, max: 599, controlsPosition: 'right' }
    },
    {
      label: 'pages.system.log.api.search.businessCode',
      key: 'businessCode',
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
      label: 'pages.system.log.api.search.costTimeMin',
      key: 'costTimeMin',
      type: 'number',
      props: { min: 0, controlsPosition: 'right' }
    },
    {
      label: 'pages.system.log.api.search.costTimeMax',
      key: 'costTimeMax',
      type: 'number',
      props: { min: 0, controlsPosition: 'right' }
    },
    {
      label: 'pages.system.log.common.search.username',
      key: 'username',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.common.search.clientIp',
      key: 'clientIp',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.common.search.traceId',
      key: 'traceId',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.api.search.apiTimeRange',
      key: 'apiTimeRange',
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

  async function handleSearch(params: ApiLogSearchForm) {
    await searchBarRef.value.validate()
    emit('search', {
      requestUri: params.requestUri,
      requestMethod: params.requestMethod,
      httpStatus: params.httpStatus,
      businessCode: params.businessCode,
      result: params.result,
      costTimeMin: params.costTimeMin,
      costTimeMax: params.costTimeMax,
      username: params.username,
      clientIp: params.clientIp,
      traceId: params.traceId,
      apiTimeStart: params.apiTimeRange?.[0],
      apiTimeEnd: params.apiTimeRange?.[1]
    })
  }
</script>
