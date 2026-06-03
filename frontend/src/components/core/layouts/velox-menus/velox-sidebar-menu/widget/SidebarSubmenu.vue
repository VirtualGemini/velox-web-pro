<template>
  <template v-for="(item, index) in filteredMenuItems" :key="getUniqueKey(item, index)">
    <ElSubMenu v-if="hasChildren(item)" :index="item.path || item.meta.title" :level="level">
      <template #title>
        <div v-if="resolveMenuIcon(item)" class="menu-icon flex-cc">
          <VeloxSvgIcon
            :icon="resolveMenuIcon(item)"
            :color="theme?.iconColor"
            :style="{ color: theme.iconColor }"
          />
        </div>
        <span class="menu-name">
          {{ formatMenuTitle(item.meta.title) }}
        </span>
        <div v-if="item.meta.showBadge" class="velox-badge" style="right: 10px" />
      </template>

      <SidebarSubmenu
        :list="item.children"
        :is-mobile="isMobile"
        :level="level + 1"
        :theme="theme"
        :parent-icon="item.meta.icon || parentIcon"
        @close="closeMenu"
      />
    </ElSubMenu>

    <ElMenuItem
      v-else
      :index="isExternalLink(item) ? undefined : item.path || item.meta.title"
      :level-item="level + 1"
      @click="goPage(item)"
    >
      <div v-if="resolveMenuIcon(item)" class="menu-icon flex-cc">
        <VeloxSvgIcon
          :icon="resolveMenuIcon(item)"
          :color="theme?.iconColor"
          :style="{ color: theme.iconColor }"
        />
      </div>
      <div
        v-show="item.meta.showBadge && level === 0 && !menuOpen"
        class="velox-badge"
        style="right: 5px"
      />

      <template #title>
        <span class="menu-name">
          {{ formatMenuTitle(item.meta.title) }}
        </span>
        <div v-if="item.meta.showBadge" class="velox-badge" />
        <div v-if="item.meta.showTextBadge && (level > 0 || menuOpen)" class="velox-text-badge">
          {{ item.meta.showTextBadge }}
        </div>
      </template>
    </ElMenuItem>
  </template>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import type { AppRouteRecord } from '@/types/router'
  import { formatMenuTitle } from '@/utils/router'
  import { handleMenuJump } from '@/utils/navigation'
  import { useSettingStore } from '@/store/modules/setting'
  import { MenuTypeEnum } from '@/enums/appEnum'

  /** 菜单项无图标时的默认兜底图标 */
  const DEFAULT_MENU_ICON = 'ri:menu-line'

  interface MenuTheme {
    iconColor?: string
  }

  interface Props {
    /** 菜单标题 */
    title?: string
    /** 菜单列表 */
    list?: AppRouteRecord[]
    /** 主题配置 */
    theme?: MenuTheme
    /** 是否为移动端模式 */
    isMobile?: boolean
    /** 菜单层级 */
    level?: number
    /** 父级菜单图标（双列菜单折叠时用于图标回退） */
    parentIcon?: string
  }

  interface Emits {
    /** 关闭菜单事件 */
    (e: 'close'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    title: '',
    list: () => [],
    theme: () => ({}),
    isMobile: false,
    level: 0
  })

  const emit = defineEmits<Emits>()

  const settingStore = useSettingStore()

  const { menuOpen, menuType, dualMenuShowText } = storeToRefs(settingStore)

  /**
   * 双列菜单「左侧文本」与「右侧菜单」是否同时折叠
   * 此状态下右侧菜单仅显示图标，需保证每个菜单项都有图标可展示
   */
  const isDualMenuFullyCollapsed = computed(
    () =>
      menuType.value === MenuTypeEnum.DUAL_MENU && !dualMenuShowText.value && !menuOpen.value
  )

  /**
   * 解析菜单项最终展示的图标
   * 双列菜单完全折叠时，无图标的菜单项回退到父级图标；父级仍无图标则使用默认图标。
   * 其它情况下保持原行为：无图标则不展示。
   * @param item 菜单项数据
   */
  const resolveMenuIcon = (item: AppRouteRecord): string => {
    if (isDualMenuFullyCollapsed.value) {
      return item.meta.icon || props.parentIcon || DEFAULT_MENU_ICON
    }
    return item.meta.icon || ''
  }

  /**
   * 过滤后的菜单项列表
   * 只显示未隐藏的菜单项
   */
  const filteredMenuItems = computed(() => filterRoutes(props.list))

  /**
   * 跳转到指定页面
   * @param item 菜单项数据
   */
  const goPage = (item: AppRouteRecord): void => {
    closeMenu()
    handleMenuJump(item)
  }

  /**
   * 关闭菜单
   * 触发父组件的关闭事件
   */
  const closeMenu = (): void => {
    emit('close')
  }

  /**
   * 判断菜单项本身是否可以作为可点击页面保留在菜单中
   */
  const isNavigableRoute = (item: AppRouteRecord): boolean => {
    return !!(
      !item.meta.isHide &&
      ((item.path && item.path.trim()) || item.meta.link || item.meta.isIframe === true) &&
      (item.component || item.meta.link || item.meta.isIframe === true)
    )
  }

  /**
   * 递归过滤菜单路由，移除隐藏的菜单项
   * 但如果父菜单本身就是可访问页面，则即使子菜单都被隐藏也应该保留
   * @param items 菜单项数组
   * @returns 过滤后的菜单项数组
   */
  const filterRoutes = (items: AppRouteRecord[]): AppRouteRecord[] => {
    return items
      .filter((item) => {
        // 如果当前项被隐藏，直接过滤掉
        if (item.meta.isHide) {
          return false
        }

        // 如果有子菜单，递归过滤子菜单
        if (item.children && item.children.length > 0) {
          const filteredChildren = filterRoutes(item.children)
          // 目录菜单要求有可见子菜单；页面菜单则允许仅保留自身
          return filteredChildren.length > 0 || isNavigableRoute(item)
        }

        // 叶子节点且未被隐藏，保留
        return isNavigableRoute(item)
      })
      .map((item) => ({
        ...item,
        children: item.children ? filterRoutes(item.children) : undefined
      }))
  }

  /**
   * 判断菜单项是否包含可见的子菜单
   * @param item 菜单项数据
   * @returns 是否包含可见的子菜单
   */
  const hasChildren = (item: AppRouteRecord): boolean => {
    if (!item.children || item.children.length === 0) {
      return false
    }
    // 递归检查是否有可见的子菜单
    const filteredChildren = filterRoutes(item.children)
    return filteredChildren.length > 0
  }

  /**
   * 判断是否为外部链接
   * @param item 菜单项数据
   * @returns 是否为外部链接
   */
  const isExternalLink = (item: AppRouteRecord): boolean => {
    return !!(item.meta.link && !item.meta.isIframe)
  }

  /**
   * 生成唯一的 key
   * 使用 path、title 和 index 组合确保唯一性
   * @param item 菜单项数据
   * @param index 索引
   * @returns 唯一的 key
   */
  const getUniqueKey = (item: AppRouteRecord, index: number): string => {
    return `${item.path || item.meta.title || 'menu'}-${props.level}-${index}`
  }
</script>
