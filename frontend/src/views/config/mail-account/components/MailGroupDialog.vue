<script setup lang="ts">
  import { computed, reactive, ref, watch, h } from 'vue'
  import { ElMessage, ElMessageBox, ElSwitch, type FormInstance, type FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import VeloxButtonTable from '@/components/core/forms/velox-button-table/index.vue'
  import VeloxTable from '@/components/core/tables/velox-table/index.vue'
  import {
    fetchMailGroupCreate,
    fetchMailGroupDelete,
    fetchMailGroupDeleteBatch,
    fetchMailGroupList,
    fetchMailGroupUpdate,
    type MailGroup
  } from '@/api/mail'

  const props = defineProps<{
    visible: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'changed'): void
  }>()

  const { t } = useI18n()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const groups = ref<MailGroup[]>([])
  const dirty = ref(false)

  const groupPage = ref(1)
  const pageSize = 5
  const selectedRows = ref<MailGroup[]>([])

  function handleSelectionChange(selection: MailGroup[]) {
    selectedRows.value = selection
  }

  const groupPagination = computed(() => ({
    current: groupPage.value,
    size: pageSize,
    total: groups.value.length
  }))

  const pagedGroups = computed(() => {
    const start = (groupPage.value - 1) * pageSize
    return groups.value.slice(start, start + pageSize)
  })

  const groupColumns = computed(() => [
    { type: 'selection' as const, width: 46 },
    {
      prop: 'name',
      label: t('pages.config.mailAccount.group.columns.name'),
      minWidth: 160,
      showOverflowTooltip: true
    },
    {
      label: t('pages.config.mailAccount.group.columns.active'),
      width: 90,
      align: 'center',
      formatter: (row: MailGroup) =>
        h(ElSwitch, {
          modelValue: row.active === 1,
          onChange: (val: string | number | boolean) => handleToggleActive(row, !!val)
        })
    },
    {
      prop: 'sort',
      label: t('pages.config.mailAccount.group.columns.sort'),
      width: 80,
      align: 'center'
    },
    {
      prop: 'accountCount',
      label: t('pages.config.mailAccount.group.columns.accountCount'),
      width: 90,
      align: 'center'
    },
    {
      prop: 'remark',
      label: t('pages.config.mailAccount.group.columns.remark'),
      minWidth: 160,
      showOverflowTooltip: true,
      formatter: (row: MailGroup) => row.remark || '-'
    },
    {
      label: t('pages.config.mailAccount.group.columns.operation'),
      width: 110,
      fixed: 'right' as const,
      formatter: (row: MailGroup) =>
        h('div', { class: 'flex items-center' }, [
          h(VeloxButtonTable, { type: 'edit', onClick: () => openEdit(row) }),
          h(VeloxButtonTable, { type: 'delete', onClick: () => handleDelete(row) })
        ])
    }
  ])

  function handleGroupPageChange(page: number) {
    groupPage.value = page
  }

  async function loadGroups() {
    loading.value = true
    try {
      groups.value = (await fetchMailGroupList()) || []
      groupPage.value = 1
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        dirty.value = false
        loadGroups()
      } else if (dirty.value) {
        emit('changed')
      }
    }
  )

  const formVisible = ref(false)
  const formMode = ref<'create' | 'edit'>('create')
  const submitting = ref(false)
  const formRef = ref<FormInstance>()
  const formModel = reactive({
    id: '',
    name: '',
    active: true,
    sort: 1,
    remark: ''
  })

  const formRules = reactive<FormRules>({
    name: [
      {
        required: true,
        message: t('pages.config.mailAccount.group.messages.enterName'),
        trigger: 'blur'
      }
    ]
  })

  function openCreate() {
    formMode.value = 'create'
    Object.assign(formModel, { id: '', name: '', active: true, sort: 1, remark: '' })
    formVisible.value = true
  }

  function openEdit(row: MailGroup) {
    formMode.value = 'edit'
    Object.assign(formModel, {
      id: row.id,
      name: row.name,
      active: row.active === 1,
      sort: row.sort ?? 1,
      remark: row.remark || ''
    })
    formVisible.value = true
  }

  function handleFormClosed() {
    formRef.value?.resetFields()
  }

  async function handleSubmit() {
    if (!formRef.value) return
    try {
      await formRef.value.validate()
    } catch {
      return
    }

    submitting.value = true
    try {
      const payload = {
        name: formModel.name.trim(),
        active: formModel.active ? 1 : 0,
        sort: formModel.sort,
        remark: formModel.remark.trim() || undefined
      }
      if (formMode.value === 'create') {
        await fetchMailGroupCreate(payload)
        ElMessage.success(t('pages.config.mailAccount.group.messages.createSuccess'))
      } else {
        await fetchMailGroupUpdate({ ...payload, id: formModel.id })
        ElMessage.success(t('pages.config.mailAccount.group.messages.updateSuccess'))
      }
      dirty.value = true
      formVisible.value = false
      loadGroups()
    } finally {
      submitting.value = false
    }
  }

  async function handleToggleActive(row: MailGroup, value: boolean) {
    const next = value ? 1 : 0
    await fetchMailGroupUpdate({
      id: row.id,
      name: row.name,
      active: next,
      sort: row.sort,
      remark: row.remark || undefined
    })
    row.active = next
    dirty.value = true
    ElMessage.success(
      next
        ? t('pages.config.mailAccount.group.messages.activeSuccess')
        : t('pages.config.mailAccount.group.messages.inactiveSuccess')
    )
  }

  async function handleDelete(row: MailGroup) {
    await ElMessageBox.confirm(
      t('pages.config.mailAccount.group.messages.confirmDelete', { name: row.name }),
      t('pages.config.mailAccount.group.messages.deleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await fetchMailGroupDelete(row.id)
    ElMessage.success(t('pages.config.mailAccount.group.messages.deleteSuccess'))
    dirty.value = true
    loadGroups()
  }

  async function handleBatchDelete() {
    const ids = selectedRows.value.map((row) => row.id)
    if (ids.length === 0) return

    await ElMessageBox.confirm(
      t('pages.config.mailAccount.group.messages.batchDeleteConfirm', { count: ids.length }),
      t('pages.config.mailAccount.group.messages.batchDeleteTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    const result = await fetchMailGroupDeleteBatch(ids)
    if (result.inUseNames && result.inUseNames.length > 0) {
      await ElMessageBox.alert(
        t('pages.config.mailAccount.group.messages.batchDeleteInUse') +
          '\n' +
          result.inUseNames.map((name) => '  • ' + name).join('\n'),
        t('pages.config.mailAccount.group.messages.batchDeleteTitle'),
        { confirmButtonText: t('pages.config.mailAccount.group.messages.iKnow'), type: 'warning' }
      )
      return
    }

    ElMessage.success(t('pages.config.mailAccount.group.messages.batchDeleteSuccess'))
    dirty.value = true
    loadGroups()
  }
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    :title="t('pages.config.mailAccount.group.title')"
    width="860px"
    align-center
    :close-on-click-modal="false"
    :lock-scroll="false"
  >
    <div class="mb-3 flex items-center gap-2">
      <ElButton @click="openCreate" v-ripple>
        {{ t('pages.config.mailAccount.group.add') }}
      </ElButton>
      <ElButton
        type="danger"
        plain
        :disabled="selectedRows.length === 0"
        @click="handleBatchDelete"
        v-ripple
      >
        {{ t('common.batchDelete') }}
      </ElButton>
    </div>

    <VeloxTable
      :loading="loading"
      :data="pagedGroups"
      :columns="groupColumns"
      :pagination="groupPagination"
      :pagination-options="{
        pageSizes: [pageSize],
        layout: 'total, prev, pager, next'
      }"
      :show-table-header="false"
      size="small"
      height="237"
      @pagination:current-change="handleGroupPageChange"
      @selection-change="handleSelectionChange"
    />

    <template #footer>
      <ElButton @click="dialogVisible = false">{{ t('common.confirm') }}</ElButton>
    </template>
  </ElDialog>

  <ElDialog
    v-model="formVisible"
    :title="
      formMode === 'create'
        ? t('pages.config.mailAccount.group.form.titles.create')
        : t('pages.config.mailAccount.group.form.titles.edit')
    "
    width="460px"
    align-center
    append-to-body
    :close-on-click-modal="false"
    :lock-scroll="false"
    @closed="handleFormClosed"
  >
    <ElForm ref="formRef" :model="formModel" :rules="formRules" label-width="80px">
      <ElFormItem :label="t('pages.config.mailAccount.group.form.fields.name')" prop="name">
        <ElInput
          v-model="formModel.name"
          maxlength="50"
          :placeholder="t('pages.config.mailAccount.group.form.placeholders.name')"
        />
      </ElFormItem>
      <ElFormItem :label="t('pages.config.mailAccount.group.form.fields.active')">
        <ElSwitch v-model="formModel.active" />
      </ElFormItem>
      <ElFormItem :label="t('pages.config.mailAccount.group.form.fields.sort')">
        <ElInputNumber v-model="formModel.sort" :min="0" :max="9999" controls-position="right" />
      </ElFormItem>
      <ElFormItem :label="t('pages.config.mailAccount.group.form.fields.remark')">
        <ElInput
          v-model="formModel.remark"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          :placeholder="t('pages.config.mailAccount.group.form.placeholders.remark')"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="formVisible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton type="primary" @click="handleSubmit">{{ t('table.form.submit') }}</ElButton>
    </template>
  </ElDialog>
</template>
