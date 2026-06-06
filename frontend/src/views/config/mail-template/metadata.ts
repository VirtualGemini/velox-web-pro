import type { MailTemplateMetadata } from '@/api/mailTemplate'

export function createEmptyMailTemplateMetadata(): MailTemplateMetadata {
  return {
    sendTypes: [],
    templateTypes: [],
    variables: [],
    validityMinutes: {},
    previewSample: {
      username: '',
      code: '',
      appName: '',
      fromAddress: '',
      toAddress: ''
    }
  }
}

export function buildPreviewVariables(
  metadata: MailTemplateMetadata,
  typeCode: string
): Record<string, string> {
  const sample = metadata.previewSample
  return {
    username: sample.username,
    code: sample.code,
    validityMinutes: String(metadata.validityMinutes[typeCode] ?? ''),
    appName: sample.appName
  }
}
