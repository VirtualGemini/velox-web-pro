/**
 * 工作标签页管理模块
 *
 * 提供工作标签页（Worktab）的自动管理功能
 *
 * ## 主要功能
 *
 * - 根据路由导航自动创建和更新工作标签页
 * - iframe 页面标签页特殊处理
 * - 标签页信息提取（标题、路径、缓存状态等）
 * - 固定标签页支持
 * - 根据系统设置控制标签页显示
 * - 首页标签页特殊处理
 *
 * ## 使用场景
 *
 * - 路由守卫中自动创建标签页
 * - 页面切换时更新标签页状态
 * - 多标签页导航系统
 *
 * @module utils/navigation/worktab
 * @author Velox Team
 */
import { useWorktabStore } from '@/store/modules/worktab'
import { RouteLocationNormalized } from 'vue-router'
import { isIframe } from './route'
import { useSettingStore } from '@/store/modules/setting'
import { IframeRouteManager } from '@/router/core'
import { useCommon } from '@/hooks/core/useCommon'

const WORKTAB_VISIBLE_HIDDEN_ROUTE_NAMES = new Set(['AccountCenter'])

const shouldShowWorktab = (to: RouteLocationNormalized): boolean => {
  if (!to.meta.isHideTab) return true
  return WORKTAB_VISIBLE_HIDDEN_ROUTE_NAMES.has(String(to.name || ''))
}

const normalizeIcon = (icon: unknown): string | undefined => {
  return typeof icon === 'string' && icon.trim() ? icon : undefined
}

/**
 * 根据当前路由信息设置工作标签页（worktab）
 * @param to 当前路由对象
 */
export const setWorktab = (to: RouteLocationNormalized): void => {
  const worktabStore = useWorktabStore()
  const { meta, path, name, params, query } = to
  const routeMeta = to.matched[to.matched.length - 1]?.meta ?? meta
  if (shouldShowWorktab(to)) {
    // 如果是 iframe 页面，则特殊处理工作标签页
    if (isIframe(path)) {
      const iframeRoute = IframeRouteManager.getInstance().findByPath(to.path)

      if (iframeRoute?.meta) {
        worktabStore.openTab({
          title: iframeRoute.meta.title,
          icon: normalizeIcon(iframeRoute.meta.icon),
          path,
          name: name as string,
          keepAlive: meta.keepAlive as boolean,
          params,
          query
        })
      }
    } else if (useSettingStore().showWorkTab || path === useCommon().homePath.value) {
      worktabStore.openTab({
        title: meta.title as string,
        icon: normalizeIcon(routeMeta.icon),
        path,
        name: name as string,
        keepAlive: meta.keepAlive as boolean,
        params,
        query,
        fixedTab: meta.fixedTab as boolean
      })
    }
  }
}
