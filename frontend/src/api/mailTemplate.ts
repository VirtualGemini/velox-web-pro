import request from '@/utils/http'

export type MailTemplateKind = 'SYSTEM' | 'CUSTOM'

export interface MailTemplatePreviewSample {
  username: string
  code: string
  appName: string
  fromAddress: string
  toAddress: string
}

export interface MailTemplate {
  id: string
  name: string
  /** 兼容旧字段：等同 sendType */
  type: string
  /** 发件类型 code，见 views/config/mail-template/constants.ts */
  sendType: string
  /** 模板类型：SYSTEM / CUSTOM */
  templateType: MailTemplateKind
  subject: string
  /** 仅详情(get)返回 */
  content?: string
  enabled: number
  sort: number
  remark: string
  createTime: string
  updateTime: string
}

export interface MailTemplateQuery {
  page?: number
  size?: number
  name?: string
  sendType?: string
  templateType?: MailTemplateKind
  enabled?: number
  remark?: string
  createTimeStart?: string
  createTimeEnd?: string
  updateTimeStart?: string
  updateTimeEnd?: string
}

export interface MailTemplatePageResult {
  records: MailTemplate[]
  total: number
  page: number
  size: number
  pages: number
}

export interface MailTemplateMetadata {
  sendTypes: string[]
  templateTypes: MailTemplateKind[]
  variables: string[]
  validityMinutes: Record<string, number>
  previewSample: MailTemplatePreviewSample
}

export interface MailTemplateSaveCommand {
  id?: string
  name: string
  /** 编辑时后端忽略 sendType（发件类型不可变），仅新增/复制生效 */
  sendType: string
  /** 新建默认 CUSTOM；系统内置模板由种子数据提供 */
  templateType?: MailTemplateKind
  subject?: string
  content?: string
  enabled?: number
  sort?: number
  remark?: string
}

export function fetchMailTemplatePage(params: MailTemplateQuery) {
  return request.get<MailTemplatePageResult>({
    url: '/api/mail-template/page',
    params
  })
}

export function fetchMailTemplateMetadata() {
  return request.get<MailTemplateMetadata>({
    url: '/api/mail-template/metadata'
  })
}

export function fetchMailTemplateGet(id: string) {
  return request.get<MailTemplate>({
    url: '/api/mail-template/get',
    params: { id }
  })
}

export function fetchMailTemplateCreate(data: MailTemplateSaveCommand) {
  return request.post<string>({
    url: '/api/mail-template/create',
    data
  })
}

export function fetchMailTemplateUpdate(data: MailTemplateSaveCommand) {
  return request.put<boolean>({
    url: '/api/mail-template/update',
    data
  })
}

export function fetchMailTemplateDelete(id: string) {
  return request.del<boolean>({
    url: '/api/mail-template/delete',
    params: { id }
  })
}

export function fetchMailTemplateDeleteBatch(ids: string[]) {
  return request.del<boolean>({
    url: '/api/mail-template/delete-batch',
    params: { ids },
    paramsSerializer: { indexes: null }
  })
}

export function fetchMailTemplateUpdateEnabled(id: string, enabled: number) {
  return request.put<boolean>({
    url: `/api/mail-template/update-enabled?id=${encodeURIComponent(id)}&enabled=${enabled}`
  })
}
