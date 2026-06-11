import request from '@/utils/http'

export type LogResult = 0 | 1

export interface BaseLogQuery {
  current?: number
  size?: number
  pageNo?: number
  pageSize?: number
  username?: string
  accountId?: string
  result?: LogResult
  clientIp?: string
  countryName?: string
  provinceName?: string
  cityName?: string
  traceId?: string
  createTimeStart?: string
  createTimeEnd?: string
}

export interface LoginLogQuery extends BaseLogQuery {
  eventType?: string
  loginMethod?: string
  mfaType?: string
  failureCode?: string
  districtName?: string
  eventTimeStart?: string
  eventTimeEnd?: string
}

export interface OperationLogQuery extends BaseLogQuery {
  moduleName?: string
  actionName?: string
  operationType?: string
  operationTimeStart?: string
  operationTimeEnd?: string
}

export interface ApiLogQuery extends BaseLogQuery {
  requestMethod?: string
  requestUri?: string
  httpStatus?: number
  businessCode?: string
  costTimeMin?: number
  costTimeMax?: number
  apiTimeStart?: string
  apiTimeEnd?: string
}

export interface LogRecord {
  id: string
  createTime?: string
  accountId?: string
  username?: string
  result?: LogResult
  traceId?: string
  clientIp?: string
  ipVersion?: string
  countryCode?: string
  countryName?: string
  provinceName?: string
  cityName?: string
  districtName?: string
  ipLocation?: string
  isp?: string
  locationSource?: string
  locationParsedAt?: string
  userAgent?: string
  browser?: string
  os?: string
  deviceType?: string
  [key: string]: unknown
}

export type LoginLogRecord = LogRecord & {
  eventType?: string
  loginMethod?: string
  mfaType?: string
  failureCode?: string
  failureMessage?: string
  sessionId?: string
  eventTime?: string
  logoutTime?: string
  firstLogin?: number
  riskType?: string
}

export type OperationLogRecord = LogRecord & {
  moduleName?: string
  actionName?: string
  operationType?: string
  targetType?: string
  targetId?: string
  operatorType?: string
  requestMethod?: string
  requestUri?: string
  javaMethod?: string
  requestParams?: string
  beforeData?: string
  afterData?: string
  responseSummary?: string
  errorCode?: string
  errorMessage?: string
  costTimeMs?: number
  operationTime?: string
}

export type ApiLogRecord = LogRecord & {
  callerApp?: string
  requestUrl?: string
  requestMethod?: string
  requestUri?: string
  matchedPattern?: string
  javaMethod?: string
  httpStatus?: number
  businessCode?: string
  businessMessage?: string
  requestQuery?: string
  requestHeaders?: string
  requestBody?: string
  responseBody?: string
  requestSize?: number
  responseSize?: number
  errorCode?: string
  errorMessage?: string
  exceptionStack?: string
  serverIp?: string
  serverNode?: string
  requestTime?: string
  responseTime?: string
  costTimeMs?: number
  apiTime?: string
}

export function fetchLoginLogList(params: LoginLogQuery) {
  return request.get<Api.Common.PaginatedResponse<LoginLogRecord>>({
    url: '/api/log/login/list',
    params
  })
}

export function fetchLoginLogDetail(id: string) {
  return request.get<LoginLogRecord>({ url: `/api/log/login/${id}` })
}

export function fetchDeleteLoginLogs(ids: string[]) {
  return request.del<boolean>({
    url: '/api/log/login/delete-batch',
    params: { ids },
    paramsSerializer: { indexes: null }
  })
}

export function fetchCleanLoginLogs() {
  return request.del<boolean>({ url: '/api/log/login/clean' })
}

export function fetchOperationLogList(params: OperationLogQuery) {
  return request.get<Api.Common.PaginatedResponse<OperationLogRecord>>({
    url: '/api/log/operation/list',
    params
  })
}

export function fetchOperationLogDetail(id: string) {
  return request.get<OperationLogRecord>({ url: `/api/log/operation/${id}` })
}

export function fetchDeleteOperationLogs(ids: string[]) {
  return request.del<boolean>({
    url: '/api/log/operation/delete-batch',
    params: { ids },
    paramsSerializer: { indexes: null }
  })
}

export function fetchCleanOperationLogs() {
  return request.del<boolean>({ url: '/api/log/operation/clean' })
}

export function fetchApiLogList(params: ApiLogQuery) {
  return request.get<Api.Common.PaginatedResponse<ApiLogRecord>>({
    url: '/api/log/api/list',
    params
  })
}

export function fetchApiLogDetail(id: string) {
  return request.get<ApiLogRecord>({ url: `/api/log/api/${id}` })
}

export function fetchDeleteApiLogs(ids: string[]) {
  return request.del<boolean>({
    url: '/api/log/api/delete-batch',
    params: { ids },
    paramsSerializer: { indexes: null }
  })
}

export function fetchCleanApiLogs() {
  return request.del<boolean>({ url: '/api/log/api/clean' })
}

export function fetchApiLogHttpStatuses() {
  return request.get<number[]>({ url: '/api/log/api/http-statuses' })
}
