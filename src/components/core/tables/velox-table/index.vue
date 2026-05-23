<!-- 表格组件 -->
<!-- 支持：el-table 全部属性、事件、插槽，同官方文档写法 -->
<!-- 扩展功能：分页组件、渲染自定义列、loading、表格全局边框、斑马纹、表格尺寸、表头背景配置 -->
<!-- 获取 ref：默认暴露了 elTableRef 外部通过 ref.value.elTableRef 可以调用 el-table 方法 -->
<template>
  <div
    class="velox-table"
    :class="{ 'is-empty': isEmpty, 'is-mobile-table': isMobileTable }"
    :style="containerHeight"
  >
    <ElTable ref="elTableRef" v-loading="!!loading" v-bind="mergedTableProps">
      <template v-for="col in columns" :key="col.prop || col.type">
        <!-- 渲染全局序号列 -->
        <ElTableColumn v-if="col.type === 'globalIndex'" v-bind="resolveColumnProps(col)">
          <template #default="{ $index }">
            <span>{{ getGlobalIndex($index) }}</span>
          </template>
        </ElTableColumn>

        <!-- 渲染展开行 -->
        <ElTableColumn v-else-if="col.type === 'expand'" v-bind="cleanColumnProps(col)">
          <template #default="{ row }">
            <component :is="col.formatter ? col.formatter(row) : null" />
          </template>
        </ElTableColumn>

        <!-- 渲染普通列 -->
        <ElTableColumn v-else v-bind="cleanColumnProps(col)">
          <template v-if="col.useHeaderSlot && col.prop" #header="headerScope">
            <slot
              :name="col.headerSlotName || `${col.prop}-header`"
              v-bind="{ ...headerScope, prop: col.prop, label: resolveColumnLabel(col) }"
            >
              {{ resolveColumnLabel(col) }}
            </slot>
          </template>
          <template v-if="col.useSlot && col.prop" #default="slotScope">
            <slot
              v-if="shouldRenderSlotScope(slotScope)"
              :name="col.slotName || col.prop"
              v-bind="{
                ...slotScope,
                prop: col.prop,
                value: col.prop ? slotScope.row[col.prop] : undefined
              }"
            />
          </template>
        </ElTableColumn>
      </template>

      <template v-if="$slots.default" #default><slot /></template>

      <template #empty>
        <div v-if="loading"></div>
        <ElEmpty v-else :description="resolvedEmptyText" :image-size="120" />
      </template>
    </ElTable>

    <div
      class="pagination custom-pagination"
      v-if="showPagination"
      :class="mergedPaginationOptions?.align"
      ref="paginationRef"
    >
      <ElPagination
        v-bind="mergedPaginationOptions"
        :total="pagination?.total"
        :disabled="loading"
        :page-size="pagination?.size"
        :current-page="pagination?.current"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, nextTick, watchEffect, getCurrentInstance, useAttrs, watch } from 'vue'
  import type { ElTable, TableProps } from 'element-plus'
  import { storeToRefs } from 'pinia'
  import { useI18n } from 'vue-i18n'
  import { ColumnOption } from '@/types'
  import { useTableStore } from '@/store/modules/table'
  import { useCommon } from '@/hooks/core/useCommon'
  import { useTableHeight } from '@/hooks/core/useTableHeight'
  import { useResizeObserver, useWindowSize } from '@vueuse/core'

  defineOptions({ name: 'VeloxTable' })

  const { width } = useWindowSize()
  const { t, te, locale } = useI18n()
  const elTableRef = ref<InstanceType<typeof ElTable> | null>(null)
  const paginationRef = ref<HTMLElement>()
  const tableHeaderRef = ref<HTMLElement>()
  const tableStore = useTableStore()
  const { isBorder, isZebra, tableSize, isFullScreen, isHeaderBackground } = storeToRefs(tableStore)

  /** 分页配置接口 */
  interface PaginationConfig {
    /** 当前页码 */
    current: number
    /** 每页显示条目个数 */
    size: number
    /** 总条目数 */
    total: number
  }

  /** 分页器配置选项接口 */
  interface PaginationOptions {
    /** 每页显示个数选择器的选项列表 */
    pageSizes?: number[]
    /** 分页器的对齐方式 */
    align?: 'left' | 'center' | 'right'
    /** 分页器的布局 */
    layout?: string
    /** 是否显示分页器背景 */
    background?: boolean
    /** 只有一页时是否隐藏分页器 */
    hideOnSinglePage?: boolean
    /** 分页器的大小 */
    size?: 'small' | 'default' | 'large'
    /** 分页器的页码数量 */
    pagerCount?: number
  }

  /** VeloxTable 组件的 Props 接口 */
  interface VeloxTableProps extends TableProps<Record<string, any>> {
    /** 加载状态 */
    loading?: boolean
    /** 列渲染配置 */
    columns?: ColumnOption[]
    /** 分页状态 */
    pagination?: PaginationConfig
    /** 分页配置 */
    paginationOptions?: PaginationOptions
    /** 空数据表格高度 */
    emptyHeight?: string
    /** 空数据时显示的文本 */
    emptyText?: string
    /** 是否开启 VeloxTableHeader，解决表格高度自适应问题 */
    showTableHeader?: boolean
  }

  const props = withDefaults(defineProps<VeloxTableProps>(), {
    columns: () => [],
    fit: true,
    showHeader: true,
    stripe: undefined,
    border: undefined,
    size: undefined,
    emptyHeight: '100%',
    emptyText: 'table.emptyText',
    showTableHeader: true
  })
  const instance = getCurrentInstance()
  const attrs = useAttrs()

  const LAYOUT = {
    MOBILE: 'prev, pager, next, sizes, jumper, total',
    IPAD: 'prev, pager, next, jumper, total',
    DESKTOP: 'total, prev, pager, next, sizes, jumper'
  }

  const layout = computed(() => {
    if (width.value < 768) {
      return LAYOUT.MOBILE
    } else if (width.value < 1024) {
      return LAYOUT.IPAD
    } else {
      return LAYOUT.DESKTOP
    }
  })

  const isMobileTable = computed(() => width.value < 768)

  // 默认分页常量
  const DEFAULT_PAGINATION_OPTIONS: PaginationOptions = {
    pageSizes: [10, 20, 30, 50, 100],
    align: 'center',
    background: true,
    layout: layout.value,
    hideOnSinglePage: false,
    size: 'default',
    pagerCount: width.value > 1200 ? 7 : 5
  }

  // 合并分页配置
  const mergedPaginationOptions = computed(() => ({
    ...DEFAULT_PAGINATION_OPTIONS,
    ...props.paginationOptions
  }))

  const resolveLocaleText = (text?: string) => {
    if (!text) return ''
    void locale.value
    return te(text) ? t(text) : text
  }

  const resolveColumnLabel = (col: ColumnOption) => resolveLocaleText(col.label)

  const resolvedEmptyText = computed(() => {
    return resolveLocaleText(props.emptyText) || t('table.emptyText')
  })

  // 边框 (优先级：props > store)
  const border = computed(() => props.border ?? isBorder.value)
  // 斑马纹
  const stripe = computed(() => props.stripe ?? isZebra.value)
  // 表格尺寸
  const size = computed(() => props.size ?? tableSize.value)
  // 数据是否为空
  const isEmpty = computed(() => props.data?.length === 0)

  const paginationHeight = ref(0)
  const tableHeaderHeight = ref(0)

  // 使用 useResizeObserver 监听分页器高度变化
  useResizeObserver(paginationRef, (entries) => {
    const entry = entries[0]
    if (entry) {
      // 使用 requestAnimationFrame 避免 ResizeObserver loop 警告
      requestAnimationFrame(() => {
        paginationHeight.value = entry.contentRect.height
      })
    }
  })

  // 使用 useResizeObserver 监听表格头部高度变化
  useResizeObserver(tableHeaderRef, (entries) => {
    const entry = entries[0]
    if (entry) {
      // 使用 requestAnimationFrame 避免 ResizeObserver loop 警告
      requestAnimationFrame(() => {
        tableHeaderHeight.value = entry.contentRect.height
      })
    }
  })

  // 分页器与表格之间的间距常量（计算属性，响应 showTableHeader 变化）
  const PAGINATION_SPACING = computed(() => (props.showTableHeader ? 6 : 15))

  // 使用表格高度计算 Hook
  const { containerHeight } = useTableHeight({
    showTableHeader: computed(() => props.showTableHeader),
    paginationHeight,
    tableHeaderHeight,
    paginationSpacing: PAGINATION_SPACING
  })

  // 表格高度逻辑
  const height = computed(() => {
    // 全屏模式下占满全屏
    if (isFullScreen.value) return '100%'
    // 空数据且非加载状态时固定高度
    if (isEmpty.value && !props.loading) return props.emptyHeight
    // 使用传入的高度
    if (props.height) return props.height
    // 默认占满容器高度
    return '100%'
  })

  // 表头背景颜色样式
  const headerCellStyle = computed(() => ({
    background: isHeaderBackground.value
      ? 'var(--el-fill-color-lighter)'
      : 'var(--default-box-color)',
    ...(props.headerCellStyle || {}) // 合并用户传入的样式
  }))

  // 只有显式传入时才覆盖 ElTable 的原生默认值，避免继承的 Boolean props 把官方默认值冲掉。
  const hasExplicitTableProp = (propName: string): boolean => {
    const rawProps = (instance?.vnode.props || {}) as Record<string, unknown>
    const kebabName = propName.replace(/[A-Z]/g, (match) => `-${match.toLowerCase()}`)
    return propName in rawProps || kebabName in rawProps
  }

  const mergedTableProps = computed(() => ({
    ...attrs,
    ...props,
    height: height.value,
    stripe: stripe.value,
    border: border.value,
    size: size.value,
    headerCellStyle: headerCellStyle.value,
    // Element Plus 默认值为 true，未显式传入时不应被 VeloxTable 覆盖成 false。
    selectOnIndeterminate: hasExplicitTableProp('selectOnIndeterminate')
      ? props.selectOnIndeterminate
      : undefined
  }))

  // 是否显示分页器
  const showPagination = computed(() => props.pagination && !isEmpty.value)

  // Element Plus 在部分场景会先用 $index = -1 进行预渲染。
  // 这对普通展示无影响，但会让 ElForm 错误注册出 lineList.-1.xxx 这类字段。
  const shouldRenderSlotScope = (slotScope: { $index?: number }) => {
    return slotScope.$index === undefined || slotScope.$index >= 0
  }

  // 清理列属性，移除插槽相关的自定义属性，确保它们不会被 ElTableColumn 错误解释
  const resolveColumnProps = (col: ColumnOption) => {
    const columnProps = { ...col, label: resolveColumnLabel(col) } as ColumnOption
    if (isMobileTable.value) {
      columnProps.align = 'left'
      columnProps.headerAlign = 'left'
    }
    return columnProps
  }

  const cleanColumnProps = (col: ColumnOption) => {
    const columnProps = resolveColumnProps(col)
    // 删除自定义的插槽控制属性
    delete columnProps.useHeaderSlot
    delete columnProps.headerSlotName
    delete columnProps.useSlot
    delete columnProps.slotName
    return columnProps
  }

  const MOBILE_CELL_HORIZONTAL_GAP = 96
  const MOBILE_MIN_COLUMN_WIDTH = 80
  const MOBILE_GUTTER_COL_SELECTOR = 'colgroup col[name="gutter"]'
  const MOBILE_HEADER_CELL_SELECTOR =
    '.el-table__header-wrapper th.el-table__cell:not(.gutter):not(.el-table__fixed-right-patch)'
  const MOBILE_BODY_CELL_SELECTOR =
    'td.el-table__cell:not(.gutter):not(.el-table__fixed-right-patch)'

  const getTableRootElement = () => {
    return (elTableRef.value as unknown as { $el?: HTMLElement })?.$el || null
  }

  const resolveDeclaredColumnWidth = (column?: ColumnOption) => {
    if (!column) return 0
    const candidate = column.width ?? column.minWidth
    if (candidate == null) return 0
    if (typeof candidate === 'number') return candidate
    const parsed = Number.parseFloat(candidate)
    return Number.isFinite(parsed) ? parsed : 0
  }

  const resetMobileColumnWidths = () => {
    const root = getTableRootElement()
    if (!root) return

    root
      .querySelectorAll<HTMLElement>(
        '.el-table__header-wrapper colgroup col, .el-table__body-wrapper colgroup col'
      )
      .forEach((col) => {
        col.style.width = ''
        col.style.minWidth = ''
      })

    root.querySelectorAll<HTMLTableColElement>(MOBILE_GUTTER_COL_SELECTOR).forEach((col) => {
      col.style.width = ''
      col.style.minWidth = ''
    })

    root
      .querySelectorAll<HTMLElement>(
        '.el-table__header-wrapper table, .el-table__body-wrapper table, .el-table__footer-wrapper table'
      )
      .forEach((table) => {
        table.style.width = ''
        table.style.minWidth = ''
      })

    root.querySelectorAll<HTMLElement>('.el-table__cell.gutter').forEach((cell) => {
      cell.style.width = ''
      cell.style.minWidth = ''
    })
  }

  const syncMobileColumnWidths = async () => {
    if (!isMobileTable.value) {
      resetMobileColumnWidths()
      return
    }

    await nextTick()

    const root = getTableRootElement()
    if (!root) return

    const headerTable = root.querySelector<HTMLTableElement>(
      '.el-table__header-wrapper .el-table__header'
    )
    const bodyTable = root.querySelector<HTMLTableElement>(
      '.el-table__body-wrapper .el-table__body'
    )
    const footerTable = root.querySelector<HTMLTableElement>(
      '.el-table__footer-wrapper .el-table__footer'
    )

    if (!headerTable || !bodyTable) return

    const headerCells = Array.from(root.querySelectorAll<HTMLElement>(MOBILE_HEADER_CELL_SELECTOR))
    const bodyRows = Array.from(
      root.querySelectorAll<HTMLTableRowElement>('.el-table__body-wrapper tbody tr')
    )

    if (!headerCells.length) return

    const maxColumnWidth = Math.max(window.innerWidth - MOBILE_CELL_HORIZONTAL_GAP, 120)
    const resolvedWidths = headerCells.map((headerCell, columnIndex) => {
      const declaredWidth = resolveDeclaredColumnWidth(props.columns?.[columnIndex])
      const headerContentWidth = Math.ceil(
        headerCell.querySelector<HTMLElement>('.cell')?.scrollWidth || headerCell.scrollWidth || 0
      )

      let bodyContentWidth = 0
      for (const row of bodyRows) {
        const rowCells = Array.from(row.querySelectorAll<HTMLElement>(MOBILE_BODY_CELL_SELECTOR))
        const cell = rowCells[columnIndex]
        if (!cell) continue
        const contentWidth = Math.ceil(
          cell.querySelector<HTMLElement>('.cell')?.scrollWidth || cell.scrollWidth || 0
        )
        if (contentWidth > bodyContentWidth) {
          bodyContentWidth = contentWidth
        }
      }

      return Math.min(
        Math.max(headerContentWidth, bodyContentWidth, declaredWidth, MOBILE_MIN_COLUMN_WIDTH),
        Math.max(maxColumnWidth, declaredWidth)
      )
    })

    const applyWidthToCols = (table: HTMLTableElement | null) => {
      if (!table) return
      const cols = Array.from(
        table.querySelectorAll<HTMLTableColElement>('colgroup col:not([name="gutter"])')
      )
      resolvedWidths.forEach((width, index) => {
        const col = cols[index]
        if (!col) return
        const widthText = `${width}px`
        col.style.width = widthText
        col.style.minWidth = widthText
      })

      table.querySelectorAll<HTMLTableColElement>(MOBILE_GUTTER_COL_SELECTOR).forEach((col) => {
        col.style.width = '0px'
        col.style.minWidth = '0px'
      })

      const totalWidth = resolvedWidths.reduce((sum, width) => sum + width, 0)
      const tableWidth = `${totalWidth}px`
      table.style.width = tableWidth
      table.style.minWidth = tableWidth
    }

    applyWidthToCols(headerTable)
    applyWidthToCols(bodyTable)
    applyWidthToCols(footerTable)

    root.querySelectorAll<HTMLElement>('.el-table__cell.gutter').forEach((cell) => {
      cell.style.width = '0px'
      cell.style.minWidth = '0px'
    })
  }

  // 分页大小变化
  const handleSizeChange = (val: number) => {
    emit('pagination:size-change', val)
  }

  // 分页当前页变化
  const handleCurrentChange = (val: number) => {
    emit('pagination:current-change', val)
    scrollToTop() // 页码改变后滚动到表格顶部
  }

  const { scrollToTop: scrollPageToTop } = useCommon()

  // 滚动表格内容到顶部，并可以联动页面滚动到顶部
  const scrollToTop = () => {
    nextTick(() => {
      elTableRef.value?.setScrollTop(0) // 滚动 ElTable 内部滚动条到顶部
      scrollPageToTop() // 调用公共 composable 滚动页面到顶部
    })
  }

  // 全局序号
  const getGlobalIndex = (index: number) => {
    if (!props.pagination) return index + 1
    const { current, size } = props.pagination
    return (current - 1) * size + index + 1
  }

  const emit = defineEmits<{
    (e: 'pagination:size-change', val: number): void
    (e: 'pagination:current-change', val: number): void
  }>()

  // 查找并绑定表格头部元素 - 使用 VueUse 优化
  const findTableHeader = () => {
    if (!props.showTableHeader) {
      tableHeaderRef.value = undefined
      return
    }

    const tableHeader = document.getElementById('velox-table-header')
    if (tableHeader) {
      tableHeaderRef.value = tableHeader
    } else {
      // 如果找不到表格头部，设置为 undefined，useElementSize 会返回 0
      tableHeaderRef.value = undefined
    }
  }

  watchEffect(
    () => {
      // 访问响应式数据以建立依赖追踪
      void props.data?.length // 追踪数据变化
      const shouldShow = props.showTableHeader

      // 只有在需要显示表格头部时才查找
      if (shouldShow) {
        nextTick(() => {
          findTableHeader()
        })
      } else {
        // 不显示时清空引用
        tableHeaderRef.value = undefined
      }
    },
    { flush: 'post' }
  )

  watch(
    [isMobileTable, () => props.data, () => props.columns],
    () => {
      syncMobileColumnWidths()
    },
    { deep: true, flush: 'post' }
  )

  defineExpose({
    scrollToTop,
    elTableRef
  })
</script>

<style lang="scss" scoped>
  @use './style';
</style>
