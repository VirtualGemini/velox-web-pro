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
  import { fetchApiLogHttpStatuses, type ApiLogQuery } from '@/api/log'
  import { HTTP_METHOD_OPTIONS, LOG_RESULT_OPTIONS } from '../constants'

  type ApiLogSearchForm = {
    requestUri?: string
    requestMethod?: string
    httpStatus?: number
    result?: 0 | 1
    username?: string
    clientIp?: string
    countryName?: string
    provinceName?: string
    cityName?: string
    apiTimeRange?: [string, string]
    recordTimeRange?: [string, string]
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

  const httpStatusOptions = ref<{ label: string; value: number }[]>([])

  async function loadHttpStatuses() {
    const statuses = await fetchApiLogHttpStatuses()
    httpStatusOptions.value = statuses.map((code) => ({ label: String(code), value: code }))
  }

  onMounted(loadHttpStatuses)

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
      type: 'select',
      props: { options: httpStatusOptions.value, clearable: true, filterable: true }
    },
    {
      label: 'pages.system.log.common.search.result',
      key: 'result',
      type: 'select',
      props: { options: LOG_RESULT_OPTIONS, clearable: true }
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

  async function handleSearch(params: ApiLogSearchForm) {
    await searchBarRef.value.validate()
    emit('search', {
      requestUri: params.requestUri,
      requestMethod: params.requestMethod,
      httpStatus: params.httpStatus,
      result: params.result,
      username: params.username,
      clientIp: params.clientIp,
      countryName: params.countryName,
      provinceName: params.provinceName,
      cityName: params.cityName,
      apiTimeStart: params.apiTimeRange?.[0],
      apiTimeEnd: params.apiTimeRange?.[1],
      createTimeStart: params.recordTimeRange?.[0],
      createTimeEnd: params.recordTimeRange?.[1]
    })
  }
</script>
