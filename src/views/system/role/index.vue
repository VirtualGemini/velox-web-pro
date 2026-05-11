<!-- 角色管理页面 -->
<template>
  <div class="art-full-height">
    <RoleSearch
      v-show="showSearchBar"
      v-model="searchForm"
      @search="handleSearch"
      @reset="resetSearchParams"
    ></RoleSearch>

    <ElCard class="art-table-card" :style="{ 'margin-top': showSearchBar ? '12px' : '0' }">
      <ArtTableHeader
        v-model:columns="columnChecks"
        v-model:showSearchBar="showSearchBar"
        :loading="loading"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-if="hasAuth('system:role:create')" @click="showDialog('add')" v-ripple>
              新增角色
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
      </ArtTable>
    </ElCard>

    <!-- 角色编辑弹窗 -->
    <RoleEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :role-data="currentRoleData"
      @submit="handleRoleSubmit"
    />

    <!-- 菜单权限弹窗 -->
    <RolePermissionDialog
      v-model="permissionDialog"
      :role-data="currentRoleData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { ButtonMoreItem } from '@/components/core/forms/art-button-more/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchCreateRole,
    fetchDeleteRole,
    fetchGetRoleList,
    fetchUpdateRole
  } from '@/api/system-manage'
  import ArtButtonMore from '@/components/core/forms/art-button-more/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import RoleSearch from './modules/role-search.vue'
  import RoleEditDialog from './modules/role-edit-dialog.vue'
  import RolePermissionDialog from './modules/role-permission-dialog.vue'
  import { ElTag, ElMessageBox } from 'element-plus'

  defineOptions({ name: 'Role' })

  type RoleListItem = Api.SystemManage.RoleListItem
  type RoleSearchFormParams = Api.SystemManage.RoleSearchParams & {
    daterange?: string[]
  }

  const { hasAuth } = useAuth()

  // 搜索表单
  const searchForm = ref<RoleSearchFormParams>({
    roleName: undefined,
    roleCode: undefined,
    description: undefined,
    enabled: undefined,
    daterange: undefined
  })

  const showSearchBar = ref(false)

  const dialogVisible = ref(false)
  const permissionDialog = ref(false)
  const currentRoleData = ref<RoleListItem | undefined>(undefined)

  const isSystemRole = (row?: RoleListItem) => row?.type === 0
  const isSuperRole = (row?: RoleListItem) => row?.roleCode === 'R_SUPER'
  const getRoleTypeTagType = (type?: number) => (type === 0 ? 'danger' : 'info')

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
    // 核心配置
    core: {
      apiFn: fetchGetRoleList,
      apiParams: {
        current: 1,
        size: 20
      },
      // 排除 apiParams 中的属性
      excludeParams: ['daterange'],
      columnsFactory: () => [
        {
          prop: 'roleId',
          label: '角色ID',
          width: 100
        },
        {
          prop: 'roleName',
          label: '角色名称',
          minWidth: 120
        },
        {
          prop: 'roleCode',
          label: '角色编码',
          minWidth: 120
        },
        {
          prop: 'type',
          label: '角色类型',
          width: 100,
          formatter: (row) =>
            h(ElTag, { type: getRoleTypeTagType(row.type) }, () =>
              row.type === 0 ? '内置' : '自定义'
            )
        },
        {
          prop: 'description',
          label: '角色描述',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'enabled',
          label: '角色状态',
          width: 100,
          formatter: (row) => {
            const statusConfig = row.enabled
              ? { type: 'success', text: '启用' }
              : { type: 'warning', text: '禁用' }
            return h(
              ElTag,
              { type: statusConfig.type as 'success' | 'warning' },
              () => statusConfig.text
            )
          }
        },
        {
          prop: 'createTime',
          label: '创建日期',
          width: 180,
          sortable: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          fixed: 'right',
          formatter: (row) =>
            h('div', [
              h(ArtButtonMore, {
                list: buildActionList(row),
                onClick: (item: ButtonMoreItem) => buttonMoreClick(item, row)
              })
            ])
        }
      ]
    }
  })

  const dialogType = ref<'add' | 'edit'>('add')

  const resolveRoleId = (row?: RoleListItem) => {
    const roleId = row?.roleId?.trim()
    if (!roleId) {
      ElMessage.error('当前角色数据缺少 roleId，请确认列表接口返回内容')
      return null
    }
    return roleId
  }

  const showDialog = (type: 'add' | 'edit', row?: RoleListItem) => {
    if (type === 'edit' && !resolveRoleId(row)) {
      return
    }
    if (type === 'edit' && isSuperRole(row)) {
      ElMessage.warning('最高权限角色不可编辑')
      return
    }
    dialogVisible.value = true
    dialogType.value = type
    currentRoleData.value = row
  }

  const handleRoleSubmit = async (payload: Api.SystemManage.RoleSaveCommand) => {
    if (dialogType.value === 'add') {
      await fetchCreateRole(payload)
      ElMessage.success('新增成功')
    } else {
      const roleId = resolveRoleId(currentRoleData.value)
      if (!roleId) return
      await fetchUpdateRole(roleId, payload)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    refreshData()
  }

  /**
   * 搜索处理
   * @param params 搜索参数
   */
  const handleSearch = (params: RoleSearchFormParams) => {
    // 处理日期区间参数，把 daterange 转换为 startTime 和 endTime
    const { daterange, ...filtersParams } = params
    const [startTime, endTime] = Array.isArray(daterange) ? daterange : [null, null]

    replaceSearchParams({ ...filtersParams, startTime, endTime })
    getData()
  }

  const buttonMoreClick = (item: ButtonMoreItem, row: RoleListItem) => {
    switch (item.key) {
      case 'permission':
        showPermissionDialog(row)
        break
      case 'edit':
        showDialog('edit', row)
        break
      case 'delete':
        deleteRole(row)
        break
    }
  }

  const showPermissionDialog = (row?: RoleListItem) => {
    permissionDialog.value = true
    currentRoleData.value = row
  }

  const buildActionList = (row: RoleListItem): ButtonMoreItem[] => {
    const items: ButtonMoreItem[] = [
      {
        key: 'permission',
        label: '菜单权限',
        icon: 'ri:user-3-line',
        auth: 'system:role:permission'
      }
    ]

    if (!isSuperRole(row)) {
      items.push({
        key: 'edit',
        label: '编辑角色',
        icon: 'ri:edit-2-line',
        auth: 'system:role:update'
      })
    }

    if (!isSystemRole(row)) {
      items.push({
        key: 'delete',
        label: '删除角色',
        icon: 'ri:delete-bin-4-line',
        color: '#f56c6c',
        auth: 'system:role:delete'
      })
    }

    return items
  }

  const deleteRole = (row: RoleListItem) => {
    if (isSystemRole(row)) {
      ElMessage.warning('系统角色不可删除')
      return
    }
    ElMessageBox.confirm(`确定删除角色"${row.roleName}"吗？此操作不可恢复！`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        const roleId = resolveRoleId(row)
        if (!roleId) return
        await fetchDeleteRole(roleId)
        ElMessage.success('删除成功')
        refreshData()
      })
      .catch(() => {
        ElMessage.info('已取消删除')
      })
  }
</script>
