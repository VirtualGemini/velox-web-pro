/**
 * 统一日志模块
 *
 * 收敛全项目 console 输出，提供分级日志与环境策略。
 *
 * ## 环境策略
 *
 * - 开发环境：全部级别输出到控制台，便于调试
 * - 生产环境：仅 error 输出到控制台；warn / error 交给可注册的上报器
 *
 * ## 分级约定
 *
 * - logger.warn：「已处理」的预期错误（已 ElMessage 提示的业务 HttpError、表单校验失败等）。
 *   生产静默，避免控制台与监控平台被合理范围内的业务噪音淹没。
 * - logger.error：真异常（5xx、网络中断、超时、非 HttpError 的代码异常）。始终可见、可上报。
 *
 * @module utils/sys/logger
 * @author Velox Team
 */

type LogLevel = 'debug' | 'info' | 'warn' | 'error'
type LogFn = (...args: unknown[]) => void
type Reporter = (level: LogLevel, args: unknown[]) => void

const isDev = import.meta.env.DEV

let reporter: Reporter | null = null

/**
 * 注册生产环境日志上报器
 * @param fn 上报回调；传入 null 可注销
 */
export function setLogReporter(fn: Reporter | null): void {
  reporter = fn
}

function write(level: LogLevel, args: unknown[]): void {
  if (isDev) {
    console[level](...args)
  } else if (level === 'error') {
    console.error(...args)
  }

  // 生产环境把 warn / error 交给上报器（如已注册）
  if (!isDev && (level === 'warn' || level === 'error')) {
    reporter?.(level, args)
  }
}

export interface Logger {
  debug: LogFn
  info: LogFn
  warn: LogFn
  error: LogFn
}

/**
 * 创建带作用域前缀的 logger
 * @param scope 作用域标签（如 '[HTTP]'），作为每条日志的统一前缀
 */
export function createLogger(scope?: string): Logger {
  const prefix = scope ? [scope] : []
  return {
    debug: (...args) => write('debug', [...prefix, ...args]),
    info: (...args) => write('info', [...prefix, ...args]),
    warn: (...args) => write('warn', [...prefix, ...args]),
    error: (...args) => write('error', [...prefix, ...args])
  }
}

/** 默认 logger（无作用域前缀） */
export const logger = createLogger()
