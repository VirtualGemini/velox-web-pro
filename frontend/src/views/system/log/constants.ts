import type { LogResult } from '@/api/log'

export const LOG_RESULT_OPTIONS = [
  { label: 'pages.system.log.common.result.success', value: 1 },
  { label: 'pages.system.log.common.result.fail', value: 0 }
] satisfies Array<{ label: string; value: LogResult }>

export const LOGIN_EVENT_TYPE_OPTIONS = [
  { label: 'pages.system.log.login.eventTypes.LOGIN', value: 'LOGIN' },
  { label: 'pages.system.log.login.eventTypes.LOGOUT', value: 'LOGOUT' },
  { label: 'pages.system.log.login.eventTypes.MFA_VERIFY', value: 'MFA_VERIFY' },
  { label: 'pages.system.log.login.eventTypes.REGISTER', value: 'REGISTER' }
]

export const LOGIN_METHOD_OPTIONS = [
  { label: 'pages.system.log.login.methods.password', value: 'password' },
  { label: 'pages.system.log.login.methods.emailCode', value: 'email_code' },
  { label: 'pages.system.log.login.methods.oauth', value: 'oauth' }
]

export const OPERATION_TYPE_OPTIONS = [
  { label: 'pages.system.log.operation.types.OTHER', value: 'OTHER' },
  { label: 'pages.system.log.operation.types.QUERY', value: 'QUERY' },
  { label: 'pages.system.log.operation.types.CREATE', value: 'CREATE' },
  { label: 'pages.system.log.operation.types.UPDATE', value: 'UPDATE' },
  { label: 'pages.system.log.operation.types.DELETE', value: 'DELETE' },
  { label: 'pages.system.log.operation.types.CLEAN', value: 'CLEAN' },
  { label: 'pages.system.log.operation.types.FORCE', value: 'FORCE' }
]

export const HTTP_METHOD_OPTIONS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'].map((method) => ({
  label: method,
  value: method
}))
