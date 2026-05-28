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
  type AccountSearchFormModel = {
    username?: string
    email?: string
    remark?: string
    status?: string
    createTimeRange?: [string, string]
    updateTimeRange?: [string, string]
  }

  interface Props {
    modelValue: AccountSearchFormModel
  }

  interface Emits {
    (e: 'update:modelValue', value: AccountSearchFormModel): void
    (e: 'search', params: Api.SystemManage.AccountSearchParams): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const statusOptions = [
    { label: 'pages.system.account.status.online', value: '1' },
    { label: 'pages.system.account.status.offline', value: '2' },
    { label: 'pages.system.account.status.abnormal', value: '3' },
    { label: 'pages.system.account.status.revoked', value: '4' }
  ]

  const formItems = computed(() => [
    {
      label: 'pages.system.account.search.username',
      key: 'username',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.account.search.email',
      key: 'email',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.account.search.status',
      key: 'status',
      type: 'select',
      props: {
        options: statusOptions
      }
    },
    {
      label: 'pages.system.account.search.remark',
      key: 'remark',
      type: 'input',
      clearable: true
    },
    {
      label: 'pages.system.account.search.createTimeRange',
      key: 'createTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.account.search.placeholders.startTime',
        endPlaceholder: 'pages.system.account.search.placeholders.endTime',
        rangeSeparator: 'pages.system.account.search.rangeSeparator'
      }
    },
    {
      label: 'pages.system.account.search.updateTimeRange',
      key: 'updateTimeRange',
      type: 'datetimerange',
      props: {
        style: { width: '100%' },
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        startPlaceholder: 'pages.system.account.search.placeholders.startTime',
        endPlaceholder: 'pages.system.account.search.placeholders.endTime',
        rangeSeparator: 'pages.system.account.search.rangeSeparator'
      }
    }
  ])

  function handleReset() {
    emit('reset')
  }

  async function handleSearch(params: AccountSearchFormModel) {
    await searchBarRef.value.validate()
    emit('search', {
      username: params.username,
      email: params.email,
      remark: params.remark,
      status: params.status,
      createTimeStart: params.createTimeRange?.[0],
      createTimeEnd: params.createTimeRange?.[1],
      updateTimeStart: params.updateTimeRange?.[0],
      updateTimeEnd: params.updateTimeRange?.[1]
    })
  }
</script>
