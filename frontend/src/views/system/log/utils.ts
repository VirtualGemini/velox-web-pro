import type { TagProps } from 'element-plus'
import type { LogResult } from '@/api/log'

export type LogTagType = TagProps['type']

export function formatFallback(value: unknown): string {
  if (value === null || value === undefined || value === '') return '-'
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
}

export function formatCostTime(value?: number): string {
  if (value === null || value === undefined) return '-'
  return `${value} ms`
}

export function formatBytes(value?: number): string {
  if (value === null || value === undefined) return '-'
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

export function getLogResultTagType(result?: LogResult): LogTagType {
  return result === 1 ? 'success' : 'danger'
}

export function getHttpStatusTagType(status?: number): LogTagType {
  if (!status) return 'info'
  if (status >= 500) return 'danger'
  if (status >= 400) return 'warning'
  return 'success'
}

export function getResultLabelKey(result?: LogResult): string {
  return result === 1
    ? 'pages.system.log.common.result.success'
    : 'pages.system.log.common.result.fail'
}
