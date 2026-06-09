<template>
  <ElDrawer
    v-model="visible"
    :title="resolveLabel(title)"
    :size="drawerSize"
    destroy-on-close
    append-to-body
    modal-class="log-drawer-modal"
    class="log-detail-drawer"
    @opened="tabTransitionReady = true"
  >
    <template v-if="record">
      <div class="log-detail-page">
        <section class="detail-hero">
          <div class="hero-identity" :class="{ 'hero-identity--no-media': !heroIcon }">
            <div v-if="heroIcon" class="hero-media">
              <div class="hero-icon">
                <VeloxSvgIcon :icon="heroIcon" />
              </div>
            </div>
            <div class="hero-copy">
              <div class="hero-title-row">
                <span v-if="heroTitlePrefix" class="hero-title">{{ heroTitlePrefix }}</span>
                <h1 class="hero-title">{{ heroTitle }}</h1>
                <ElTag :type="resultTagType">{{ resultText }}</ElTag>
              </div>
              <p v-if="heroSubtitle" class="hero-subtitle">{{ heroSubtitle }}</p>
            </div>
          </div>

          <div v-if="heroRows.length" class="hero-meta-list">
            <div v-for="row in heroRows" :key="row.label" class="detail-field">
              <span class="detail-label">{{ resolveLabel(row.label) }}</span>
              <span class="detail-value">{{ formatRow(row) }}</span>
            </div>
          </div>
        </section>

        <div
          ref="detailTabsShellRef"
          class="tab-scroll-shell"
          :class="{ 'is-scrollable': detailTabsScrollable }"
          @click.capture="detailTabsDrag.preventClickAfterDrag"
          @pointercancel="detailTabsDrag.endDrag"
          @pointerdown="startDetailTabsDrag"
          @pointermove="detailTabsDrag.moveDrag"
          @pointerup="detailTabsDrag.endDrag"
        >
          <ElButton
            v-if="detailTabsScrollable"
            :icon="ArrowLeft"
            :disabled="!canScrollDetailTabsLeft"
            link
            size="small"
            class="tab-scroll-nav tab-scroll-nav--left"
            aria-label="向左滚动详情标签"
            @click="scrollDetailTabs('left')"
          />
          <ElTabs
            v-model="activeTab"
            class="detail-tabs tab-scroll-tabs"
            :class="{
              'is-dragging': detailTabsDrag.isDragging.value,
              'tabs-no-anim': !tabTransitionReady
            }"
          >
            <ElTabPane
              v-for="tab in detailTabs"
              :key="tab.name"
              :label="resolveLabel(tab.label)"
              :name="tab.name"
            >
              <div class="detail-list">
                <div
                  v-for="row in tab.rows"
                  :key="row.label"
                  class="detail-field"
                  :class="{ 'detail-field--full': row.full }"
                >
                  <span class="detail-label">{{ resolveLabel(row.label) }}</span>
                  <pre v-if="row.code" class="detail-value detail-code">{{ formatRow(row) }}</pre>
                  <ElTag
                    v-else-if="row.tag"
                    class="detail-tag"
                    :type="resolveTagType(row)"
                    effect="plain"
                  >
                    {{ formatRow(row) }}
                  </ElTag>
                  <span v-else class="detail-value" :class="{ 'detail-value--long': row.full }">
                    {{ formatRow(row) }}
                  </span>
                </div>
              </div>
            </ElTabPane>
          </ElTabs>
          <ElButton
            v-if="detailTabsScrollable"
            :icon="ArrowRight"
            :disabled="!canScrollDetailTabsRight"
            link
            size="small"
            class="tab-scroll-nav tab-scroll-nav--right"
            aria-label="向右滚动详情标签"
            @click="scrollDetailTabs('right')"
          />
        </div>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { useWindowSize } from '@vueuse/core'
  import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
  import type { TagProps } from 'element-plus'
  import { useHorizontalDragScroll } from '@/hooks'
  import { useI18n } from 'vue-i18n'
  import { formatFallback, getLogResultTagType, getResultLabelKey } from '../utils'

  export interface DetailField {
    label: string
    prop: string
    code?: boolean
    tag?: boolean
    full?: boolean
    section?: string
    formatter?: (record?: Record<string, unknown>) => string
    tagType?: (record?: Record<string, unknown>) => TagProps['type']
  }

  export interface DetailTab {
    name: string
    label: string
  }

  const visible = defineModel<boolean>('visible', { default: false })
  const { t, te, locale } = useI18n()
  const { width } = useWindowSize()

  const props = withDefaults(
    defineProps<{
      title: string
      record?: Record<string, unknown>
      fields: DetailField[]
      tabs?: DetailTab[]
      icon?: string
      heroTitleProp?: string
      heroTitleFormatter?: (record?: Record<string, unknown>) => string
      heroTitlePrefixProp?: string
      heroSubtitleProp?: string
    }>(),
    {
      heroTitleProp: 'username'
    }
  )

  const activeTab = ref('overview')
  const tabTransitionReady = ref(false)
  const detailTabsShellRef = ref<HTMLElement>()
  const detailTabsScrollable = ref(false)
  const canScrollDetailTabsLeft = ref(false)
  const canScrollDetailTabsRight = ref(false)
  const detailTabsDrag = useHorizontalDragScroll()

  const drawerSize = computed(() => {
    if (width.value <= 640) return '100%'
    if (width.value <= 960) return '86%'
    return '760px'
  })

  const heroIcon = computed(() => props.icon)
  const heroTitle = computed(() =>
    props.heroTitleFormatter
      ? props.heroTitleFormatter(props.record)
      : formatFallback(props.record?.[props.heroTitleProp])
  )
  const heroTitlePrefix = computed(() =>
    props.heroTitlePrefixProp ? formatFallback(props.record?.[props.heroTitlePrefixProp]) : ''
  )
  const heroSubtitle = computed(() =>
    props.heroSubtitleProp ? formatFallback(props.record?.[props.heroSubtitleProp]) : ''
  )
  const resultTagType = computed(() =>
    getLogResultTagType(props.record?.result as 0 | 1 | undefined)
  )
  const resultText = computed(() => t(getResultLabelKey(props.record?.result as 0 | 1 | undefined)))
  const heroRows = computed(() => props.fields.filter((field) => field.section === 'hero'))
  const tabDefinitions = computed<DetailTab[]>(() =>
    props.tabs?.length
      ? props.tabs
      : [
          { name: 'overview', label: 'pages.system.log.common.sections.overview' },
          { name: 'request', label: 'pages.system.log.common.sections.request' },
          { name: 'environment', label: 'pages.system.log.common.sections.environment' }
        ]
  )
  const detailTabs = computed(() =>
    tabDefinitions.value
      .map((tab) => ({
        ...tab,
        rows: props.fields.filter((field) =>
          tab.name === 'overview'
            ? !field.section || field.section === 'overview'
            : field.section === tab.name
        )
      }))
      .filter((tab) => tab.rows.length > 0)
  )
  const firstDetailTabName = computed(() => detailTabs.value[0]?.name || 'overview')

  watch(
    () => visible.value,
    (nextVisible) => {
      if (!nextVisible) {
        activeTab.value = firstDetailTabName.value
        tabTransitionReady.value = false
      }
    }
  )

  watch(
    () => [
      visible.value,
      firstDetailTabName.value,
      detailTabs.value.map((tab) => tab.name).join('|')
    ],
    () => {
      if (!detailTabs.value.some((tab) => tab.name === activeTab.value)) {
        activeTab.value = firstDetailTabName.value
      }
    },
    { immediate: true }
  )

  watch(
    () => [visible.value, activeTab.value, width.value],
    () => nextTick(updateDetailTabsScrollState),
    { immediate: true }
  )

  function resolveLabel(label: string) {
    void locale.value
    return te(label) ? t(label) : label
  }

  function formatRow(row: DetailField) {
    if (row.formatter) return row.formatter(props.record)
    return formatFallback(props.record?.[row.prop])
  }

  function resolveTagType(row: DetailField) {
    return row.tagType?.(props.record) || 'info'
  }

  function getTabsScroller(shell?: HTMLElement) {
    return shell?.querySelector<HTMLElement>('.el-tabs__nav-scroll')
  }

  function updateDetailTabsScrollState() {
    const tabsScroller = getTabsScroller(detailTabsShellRef.value)
    if (!tabsScroller) {
      detailTabsScrollable.value = false
      canScrollDetailTabsLeft.value = false
      canScrollDetailTabsRight.value = false
      return
    }

    const maxScrollLeft = tabsScroller.scrollWidth - tabsScroller.clientWidth
    detailTabsScrollable.value = maxScrollLeft > 1
    canScrollDetailTabsLeft.value = tabsScroller.scrollLeft > 1
    canScrollDetailTabsRight.value = tabsScroller.scrollLeft < maxScrollLeft - 1
  }

  function scrollDetailTabs(direction: 'left' | 'right') {
    const tabsScroller = getTabsScroller(detailTabsShellRef.value)
    if (!tabsScroller) return
    const distance = Math.max(tabsScroller.clientWidth * 0.8, 96)
    tabsScroller.scrollBy({
      left: direction === 'left' ? -distance : distance,
      behavior: 'smooth'
    })
    window.setTimeout(updateDetailTabsScrollState, 260)
  }

  function startDetailTabsDrag(event: PointerEvent) {
    detailTabsDrag.startDrag(
      event,
      getTabsScroller(detailTabsShellRef.value),
      updateDetailTabsScrollState
    )
  }
</script>

<style scoped>
  .log-detail-drawer :deep(.el-drawer__body) {
    padding: 0;
  }

  .log-detail-page {
    width: min(100%, 760px);
    height: 100%;
    padding: 20px 24px 28px;
    margin: 0 auto;
    overflow-y: auto;
  }

  .detail-hero {
    padding-bottom: 8px;
  }

  .hero-identity {
    display: grid;
    grid-template-columns: 80px minmax(0, 1fr);
    gap: 16px;
    align-items: center;
  }

  .detail-hero .hero-identity--no-media {
    grid-template-columns: minmax(0, 1fr);
  }

  .detail-hero .hero-identity--no-media .hero-copy {
    min-height: auto;
  }

  .hero-media {
    display: flex;
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    min-width: 0;
  }

  .hero-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    border-radius: 8px;
    background: var(--el-fill-color-light);
    color: var(--velox-text-color);
    font-size: 32px;
  }

  .hero-copy {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
    min-height: 80px;
  }

  .hero-title-row {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }

  .hero-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    line-height: 32px;
    color: var(--velox-text-color);
    overflow-wrap: anywhere;
  }

  .hero-subtitle {
    margin: 8px 0 0;
    font-size: 14px;
    line-height: 22px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
    overflow-wrap: anywhere;
  }

  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    min-width: 0;
  }

  .hero-meta-list,
  .detail-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 28px;
  }

  .hero-meta-list {
    margin-top: 18px;
  }

  .detail-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 0;
    padding: 13px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .detail-field--full {
    grid-column: 1 / -1;
  }

  .detail-label {
    font-size: 12px;
    line-height: 18px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  .detail-value {
    min-width: 0;
    line-height: 22px;
    color: var(--velox-text-color);
    word-break: break-word;
    overflow-wrap: anywhere;
  }

  .detail-value--long {
    line-height: 24px;
    white-space: pre-wrap;
  }

  .detail-code {
    max-height: 240px;
    margin: 0;
    padding: 10px 12px;
    overflow: auto;
    white-space: pre-wrap;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: var(--el-border-radius-base);
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 12px;
    line-height: 1.5;
  }

  .detail-tag {
    align-self: flex-start;
    max-width: 100%;
    height: auto;
    min-height: 24px;
  }

  .detail-tag :deep(.el-tag__content) {
    line-height: 18px;
    white-space: normal;
    overflow-wrap: anywhere;
  }

  .detail-tabs {
    margin-top: 14px;
  }

  .tab-scroll-shell {
    position: relative;
  }

  .tab-scroll-shell.is-scrollable .tab-scroll-tabs :deep(.el-tabs__header) {
    padding-right: 28px;
    padding-left: 28px;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-wrap.is-scrollable) {
    padding: 0;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-prev),
  .tab-scroll-tabs :deep(.el-tabs__nav-next) {
    display: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-scroll) {
    overflow-x: auto;
    scrollbar-width: none;
    touch-action: pan-y;
    cursor: grab;
    user-select: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav-scroll::-webkit-scrollbar) {
    display: none;
  }

  .tab-scroll-tabs :deep(.el-tabs__nav) {
    flex-wrap: nowrap;
    transform: none !important;
  }

  .tab-scroll-tabs.is-dragging :deep(.el-tabs__nav-scroll) {
    cursor: grabbing;
  }

  .tab-scroll-nav {
    position: absolute;
    top: 0;
    z-index: 2;
    width: 24px;
    height: 32px;
    padding: 0;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  .tab-scroll-nav--left {
    left: 0;
  }

  .tab-scroll-nav--right {
    right: 0;
  }

  .tab-scroll-nav.is-disabled {
    opacity: 0.35;
  }

  .detail-tabs :deep(.el-tabs__header) {
    margin-bottom: 2px;
  }

  .tabs-no-anim :deep(.el-tabs__item) {
    transition: none !important;
  }

  .empty-text {
    line-height: 24px;
    color: var(--velox-text-color-secondary, var(--el-text-color-secondary));
  }

  @media (width <= 640px) {
    .log-detail-page {
      width: 100%;
      padding: 16px 16px 24px;
    }

    .hero-identity {
      grid-template-columns: 64px minmax(0, 1fr);
      gap: 12px;
    }

    .hero-icon {
      width: 64px;
      height: 64px;
      font-size: 28px;
    }

    .hero-copy {
      min-height: 64px;
    }

    .hero-title {
      font-size: 20px;
      line-height: 28px;
    }

    .hero-meta-list,
    .detail-list {
      grid-template-columns: 1fr;
    }
  }
</style>
