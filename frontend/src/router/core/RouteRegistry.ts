/**
 * 路由注册核心类
 *
 * 负责动态路由的注册、验证和管理
 *
 * @module router/core/RouteRegistry
 * @author Velox Team
 */

import type { Router, RouteRecordRaw } from 'vue-router'
import type { AppRouteRecord } from '@/types/router'
import { ComponentLoader } from './ComponentLoader'
import { RouteValidator } from './RouteValidator'
import { RouteTransformer } from './RouteTransformer'
import { logger } from '@/utils/sys/logger'

export class RouteRegistry {
  private router: Router
  private componentLoader: ComponentLoader
  private validator: RouteValidator
  private transformer: RouteTransformer
  private removeRouteFns: (() => void)[] = []
  private registered = false

  constructor(router: Router) {
    this.router = router
    this.componentLoader = new ComponentLoader()
    this.validator = new RouteValidator()
    this.transformer = new RouteTransformer(this.componentLoader)
  }

  /**
   * 注册动态路由
   */
  register(menuList: AppRouteRecord[]): void {
    if (this.registered) {
      logger.warn('[RouteRegistry] 路由已注册，跳过重复注册')
      return
    }

    // 输出非致命的诊断告警（重复路由名/组件、嵌套 Layout 误用等），仅记录不中断流程
    const { warnings } = this.validator.validate(menuList)
    warnings.forEach((msg) => logger.warn(`[RouteRegistry] 路由配置告警: ${msg}`))

    // 清洗路由：剔除无法注册的非法菜单（缺少 component 且非外链/iframe/目录），
    // 不再因单条坏数据中断整个注册流程，避免一条菜单导致整站 500 不可用
    const { routes: sanitizedMenuList, removed } = this.validator.sanitizeRoutes(menuList)
    if (removed.length > 0) {
      logger.warn(
        `[RouteRegistry] 已跳过 ${removed.length} 条无法注册的非法菜单（不影响其余菜单正常使用）:\n` +
          removed
            .map(
              (item) =>
                `  - ${item.title} (name: ${item.name || '-'}, path: ${item.path || '-'}) — ${item.reason}`
            )
            .join('\n')
      )
    }

    // 转换并注册路由（逐条容错：单条菜单注册失败仅跳过该条，不影响其余菜单）
    const removeRouteFns: (() => void)[] = []

    sanitizedMenuList.forEach((route) => {
      if (!route.name || this.router.hasRoute(route.name)) {
        return
      }
      try {
        const routeConfig = this.transformer.transform(route)
        const removeRouteFn = this.router.addRoute(routeConfig as RouteRecordRaw)
        removeRouteFns.push(removeRouteFn)
      } catch (error) {
        logger.error(
          `[RouteRegistry] 菜单注册失败，已跳过该条（name: ${String(route.name)}, path: ${route.path}）:`,
          error
        )
      }
    })

    this.removeRouteFns = removeRouteFns
    this.registered = true
  }

  /**
   * 移除所有动态路由
   */
  unregister(): void {
    this.removeRouteFns.forEach((fn) => fn())
    this.removeRouteFns = []
    this.registered = false
  }

  /**
   * 检查是否已注册
   */
  isRegistered(): boolean {
    return this.registered
  }

  /**
   * 获取移除函数列表（用于 store 管理）
   */
  getRemoveRouteFns(): (() => void)[] {
    return this.removeRouteFns
  }

  /**
   * 标记为已注册（用于错误处理场景，避免重复请求）
   */
  markAsRegistered(): void {
    this.registered = true
  }
}
