/**
 * 全局错误处理模块
 *
 * 提供统一的错误捕获和处理机制
 *
 * ## 主要功能
 *
 * - Vue 运行时错误捕获（组件错误、生命周期错误等）
 * - 全局脚本错误捕获（语法错误、运行时错误等）
 * - Promise 未捕获错误处理（unhandledrejection）
 * - 静态资源加载错误监控（图片、脚本、样式等）
 * - 错误日志记录和上报
 * - 统一的错误处理入口
 *
 * ## 使用场景
 * - 应用启动时安装全局错误处理器
 * - 捕获和记录所有类型的错误
 * - 错误上报到监控平台
 * - 提升应用稳定性和可维护性
 * - 问题排查和调试
 *
 * ## 错误类型
 *
 * - VueError: Vue 组件相关错误
 * - ScriptError: JavaScript 脚本错误
 * - PromiseError: Promise 未捕获的 rejection
 * - ResourceError: 静态资源加载失败
 *
 * @module utils/sys/error-handle
 * @author Velox Team
 */
import type { App } from 'vue'
import { isHttpError } from '@/utils/http/error'
import { logger } from './logger'

const IGNORABLE_SCRIPT_ERRORS = [
  'ResizeObserver loop completed with undelivered notifications.',
  'ResizeObserver loop limit exceeded'
]

function normalizeErrorMessage(message: Event | string): string {
  if (typeof message === 'string') {
    return message
  }

  if ('message' in message && typeof message.message === 'string') {
    return message.message
  }

  return ''
}

function isIgnorableScriptError(message: Event | string, source?: string): boolean {
  const normalizedMessage = normalizeErrorMessage(message)

  if (!normalizedMessage) {
    return false
  }

  if (IGNORABLE_SCRIPT_ERRORS.some((item) => normalizedMessage.includes(item))) {
    // 浏览器/扩展在布局抖动时常见的 ResizeObserver 噪声，不作为真实异常处理
    return true
  }

  // 浏览器扩展注入脚本偶发的跨域 Script error 也没有排查价值
  if (normalizedMessage === 'Script error.' && source === '') {
    return true
  }

  return false
}

/**
 * 识别 element-plus 表单校验失败对象
 *
 * element-plus 的 `validate()` 失败时 reject 出形如
 * `{ field: [{ message, field }, ...], ... }` 的对象。这类失败属正常交互分支，
 * 不应计为错误。作为兜底安全网（主修在调用处分离校验）。
 */
function isFormValidationError(value: unknown): boolean {
  if (!value || typeof value !== 'object' || value instanceof Error) {
    return false
  }
  const groups = Object.values(value as Record<string, unknown>)
  if (groups.length === 0) {
    return false
  }
  return groups.every(
    (group) =>
      Array.isArray(group) &&
      group.length > 0 &&
      group.every((item) => !!item && typeof item === 'object' && 'message' in item)
  )
}

/**
 * Vue 运行时错误处理
 */
export function vueErrorHandler(err: unknown, instance: any, info: string) {
  // 已被 HTTP 层提示并按级别记录，视为已处理，避免二次红报
  if (isHttpError(err)) {
    logger.warn('[VueError] 已处理的请求错误，忽略：', err.message)
    return
  }
  // 表单校验失败属正常交互分支，不计为错误
  if (isFormValidationError(err)) {
    return
  }
  logger.error('[VueError]', err, info, instance)
  // 这里可以上报到服务端，比如：
  // reportError({ type: 'vue', err, info })
}

/**
 * 全局脚本错误处理
 */
export function scriptErrorHandler(
  message: Event | string,
  source?: string,
  lineno?: number,
  colno?: number,
  error?: Error
): boolean {
  if (isIgnorableScriptError(message, source)) {
    return true
  }

  logger.error('[ScriptError]', { message, source, lineno, colno, error })
  // reportError({ type: 'script', message, source, lineno, colno, error })
  return true // 阻止默认控制台报错，可根据需求改
}

/**
 * Promise 未捕获错误处理
 */
export function registerPromiseErrorHandler() {
  window.addEventListener('unhandledrejection', (event) => {
    const reason = event.reason
    // 已处理的请求错误：HTTP 层已提示并记录，阻止浏览器默认的 "Uncaught (in promise)" 并忽略
    if (isHttpError(reason)) {
      event.preventDefault()
      logger.warn('[PromiseError] 已处理的请求错误，忽略：', reason.message)
      return
    }
    // 表单校验失败属正常交互分支，不计为错误
    if (isFormValidationError(reason)) {
      event.preventDefault()
      return
    }
    logger.error('[PromiseError]', reason)
    // reportError({ type: 'promise', reason: event.reason })
  })
}

/**
 * 资源加载错误处理 (img, script, css...)
 */
export function registerResourceErrorHandler() {
  window.addEventListener(
    'error',
    (event: Event) => {
      const target = event.target as HTMLElement
      if (
        target &&
        (target.tagName === 'IMG' || target.tagName === 'SCRIPT' || target.tagName === 'LINK')
      ) {
        logger.error('[ResourceError]', {
          tagName: target.tagName,
          src:
            (target as HTMLImageElement).src ||
            (target as HTMLScriptElement).src ||
            (target as HTMLLinkElement).href
        })
        // reportError({ type: 'resource', target })
      }
    },
    true // 捕获阶段才能监听到资源错误
  )
}

/**
 * 安装统一错误处理
 */
export function setupErrorHandle(app: App) {
  app.config.errorHandler = vueErrorHandler
  window.onerror = scriptErrorHandler
  registerPromiseErrorHandler()
  registerResourceErrorHandler()
}
