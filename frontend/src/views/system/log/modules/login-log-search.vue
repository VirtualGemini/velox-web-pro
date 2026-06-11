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
  import type { LoginLogQuery } from '@/api/log'
  import { LOG_RESULT_OPTIONS, LOGIN_EVENT_TYPE_OPTIONS, LOGIN_METHOD_OPTIONS } from '../constants'

  type LoginLogSearchForm = {
    username?: string
    eventType?: string
    loginMethod?: string
    result?: 0 | 1
    clientIp?: string
    countryName?: string
    provinceName?: string
    cityName?: string
    eventTimeRange?: [string, string]
    recordTimeRange?: [string, string]
  }

  interface Props {
    modelValue: LoginLogSearchForm
  }

  interface Emits {
    (e: 'update:modelValue', value: LoginLogSearchForm): void
    (e: 'search', params: LoginLogQuery): void
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
      label: 'pages.system.log.login.search.username',
      key: 'username',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.log.login.search.eventType',
      key: 'eventType',
      type: 'select',
      props: { options: LOGIN_EVENT_TYPE_OPTIONS, clearable: true }
    },
    {
      label: 'pages.system.log.login.search.loginMethod',
      key: 'loginMethod',
      type: 'select',
      props: { options: LOGIN_METHOD_OPTIONS, clearable: true }
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
      label: 'pages.system.log.login.search.eventTimeRange',
      key: 'eventTimeRange',
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

  async function handleSearch(params: LoginLogSearchForm) {
    await searchBarRef.value.validate()
    emit('search', {
      username: params.username,
      eventType: params.eventType,
      loginMethod: params.loginMethod,
      result: params.result,
      clientIp: params.clientIp,
      countryName: params.countryName,
      provinceName: params.provinceName,
      cityName: params.cityName,
      eventTimeStart: params.eventTimeRange?.[0],
      eventTimeEnd: params.eventTimeRange?.[1],
      createTimeStart: params.recordTimeRange?.[0],
      createTimeEnd: params.recordTimeRange?.[1]
    })
  }
</script>
