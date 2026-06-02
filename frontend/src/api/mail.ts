import request from '@/utils/http'

/** 健康状态：0-正常 1-异常 2-死亡 */
export type MailHealthStatus = 0 | 1 | 2

export interface MailAccount {
  id: string
  name: string
  groupId: string
  groupName: string
  channelId: string
  channelName: string
  protocol: string
  username: string
  /** 是否已设置密码/授权码（响应不回传明文） */
  passwordSet: boolean
  fromAddress: string
  fromName: string
  host: string
  port: number | null
  /** 0-否 1-是 null-自动识别 */
  sslEnabled: number | null
  /** 0-否 1-是 null-自动识别 */
  starttls: number | null
  weight: number
  failThreshold: number
  retryInterval: number
  maxUnavailable: number
  healthStatus: MailHealthStatus
  usageCount: number
  failCount: number
  unavailableCount: number
  nextRetryTime: string
  lastUsedTime: string
  enabled: number
  remark: string
  createTime: string
  updateTime: string
}

export interface MailAccountQuery {
  page?: number
  size?: number
  name?: string
  groupId?: string
  channelId?: string
  healthStatus?: number
  enabled?: number
  createTimeStart?: string
  createTimeEnd?: string
  updateTimeStart?: string
  updateTimeEnd?: string
}

export interface MailAccountPageResult {
  records: MailAccount[]
  total: number
  page: number
  size: number
  pages: number
}

export interface MailAccountSaveCommand {
  id?: string
  name: string
  groupId: string
  channelId: string
  username: string
  /** 新增必填；编辑留空表示保留原值 */
  password?: string
  fromAddress?: string
  fromName?: string
  host?: string
  port?: number | null
  sslEnabled?: number | null
  starttls?: number | null
  weight?: number
  failThreshold?: number
  retryInterval?: number
  maxUnavailable?: number
  enabled?: number
  remark?: string
}

export interface MailGroup {
  id: string
  name: string
  active: number
  sort: number
  accountCount: number
  remark: string
  createTime: string
  updateTime: string
}

export interface MailGroupSaveCommand {
  id?: string
  name: string
  active?: number
  sort?: number
  remark?: string
}

export interface MailChannel {
  id: string
  name: string
  protocol: string
  active: number
  sort: number
  accountCount: number
  remark: string
}

/* -------------------------------- 发件邮箱 -------------------------------- */

export function fetchMailAccountPage(params: MailAccountQuery) {
  return request.get<MailAccountPageResult>({
    url: '/api/mail-account/page',
    params
  })
}

export function fetchMailAccountGet(id: string) {
  return request.get<MailAccount>({
    url: '/api/mail-account/get',
    params: { id }
  })
}

export function fetchMailAccountCreate(data: MailAccountSaveCommand) {
  return request.post<string>({
    url: '/api/mail-account/create',
    data
  })
}

export function fetchMailAccountUpdate(data: MailAccountSaveCommand) {
  return request.put<boolean>({
    url: '/api/mail-account/update',
    data
  })
}

export function fetchMailAccountDelete(id: string) {
  return request.del<boolean>({
    url: '/api/mail-account/delete',
    params: { id }
  })
}

export function fetchMailAccountDeleteBatch(ids: string[]) {
  return request.del<boolean>({
    url: '/api/mail-account/delete-batch',
    params: { ids },
    paramsSerializer: { indexes: null }
  })
}

export function fetchMailAccountUpdateEnabled(id: string, enabled: number) {
  return request.put<boolean>({
    url: `/api/mail-account/update-enabled?id=${encodeURIComponent(id)}&enabled=${enabled}`
  })
}

export function fetchMailAccountRecover(id: string) {
  return request.put<boolean>({
    url: `/api/mail-account/recover?id=${encodeURIComponent(id)}`
  })
}

export function fetchMailAccountTest(id: string, toEmail: string) {
  return request.post<boolean>({
    url: '/api/mail-account/test',
    params: { id, toEmail }
  })
}

/* -------------------------------- 发件分组 -------------------------------- */

export function fetchMailGroupList() {
  return request.get<MailGroup[]>({
    url: '/api/mail-group/list'
  })
}

export function fetchMailGroupCreate(data: MailGroupSaveCommand) {
  return request.post<string>({
    url: '/api/mail-group/create',
    data
  })
}

export function fetchMailGroupUpdate(data: MailGroupSaveCommand) {
  return request.put<boolean>({
    url: '/api/mail-group/update',
    data
  })
}

export function fetchMailGroupDelete(id: string) {
  return request.del<boolean>({
    url: '/api/mail-group/delete',
    params: { id }
  })
}

/* -------------------------------- 发件渠道 -------------------------------- */

export function fetchMailChannelList() {
  return request.get<MailChannel[]>({
    url: '/api/mail-channel/list'
  })
}

export function fetchMailChannelUpdateActive(activeChannelIds: string[]) {
  return request.put<boolean>({
    url: '/api/mail-channel/update-active',
    data: { activeChannelIds }
  })
}
