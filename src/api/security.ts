import request from '@/utils/http'

export function fetchSecurityStatus() {
  return request.get<Api.User.Security.SecurityStatus>({
    url: '/api/user/security/status'
  })
}

export function fetchSendEmailRebindCode(data: { newEmail: string }) {
  return request.post<void>({
    url: '/api/user/security/email/rebind/send-code',
    data
  })
}

export function fetchRebindEmail(data: Api.User.Security.EmailRebindCommand) {
  return request.put<boolean>({
    url: '/api/user/security/email/rebind',
    data
  })
}

export function fetchUpdateLoginMethods(data: Api.User.Security.LoginMethodsUpdateCommand) {
  return request.put<boolean>({
    url: '/api/user/security/login-methods',
    data
  })
}

export function fetchSendMfaEmailCode() {
  return request.post<void>({
    url: '/api/user/security/mfa/email/send-code'
  })
}

export function fetchUpdateMfaEmail(data: Api.User.Security.MfaEmailUpdateCommand) {
  return request.put<boolean>({
    url: '/api/user/security/mfa/email',
    data
  })
}
