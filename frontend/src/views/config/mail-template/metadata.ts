import type { MailTemplateLanguage, MailTemplateMetadata } from '@/api/mailTemplate'

export function createEmptyMailTemplateMetadata(): MailTemplateMetadata {
  return {
    sendTypes: [],
    templateTypes: [],
    languages: [],
    variables: [],
    validityMinutes: {},
    previewSample: {
      usernameZh: '',
      usernameEn: '',
      code: '',
      appName: '',
      fromAddress: '',
      toAddress: ''
    }
  }
}

export function buildPreviewVariables(
  metadata: MailTemplateMetadata,
  typeCode: string,
  language: MailTemplateLanguage
): Record<string, string> {
  const sample = metadata.previewSample
  return {
    username: language === 'en' ? sample.usernameEn : sample.usernameZh,
    code: sample.code,
    validityMinutes: String(metadata.validityMinutes[typeCode] ?? ''),
    appName: sample.appName
  }
}
