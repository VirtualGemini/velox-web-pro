import request from '@/utils/http'

/** 访问控制全局配置 */
export interface AccessControlConfig {
  generalRegisterEnabled: boolean
  forgotPasswordEnabled: boolean
  loginMethods: string[]
  thirdPartyLoginChannels: string[]
  thirdPartyRegisterChannels: string[]
}

/** 获取访问控制配置 */
export function fetchGetAccessControl() {
  return request.get<AccessControlConfig>({
    url: '/api/settings/access-control'
  })
}

/** 更新通用注册开关 */
export function fetchUpdateGeneralRegister(enabled: boolean) {
  return request.put<boolean>({
    url: '/api/settings/access-control/general-register',
    data: { enabled }
  })
}

/** 更新忘记密码开关 */
export function fetchUpdateForgotPassword(enabled: boolean) {
  return request.put<boolean>({
    url: '/api/settings/access-control/forgot-password',
    data: { enabled }
  })
}

/** 更新普通登录方式 */
export function fetchUpdateLoginMethods(values: string[]) {
  return request.put<boolean>({
    url: '/api/settings/access-control/login-method',
    data: { values }
  })
}

/** 更新第三方登录渠道 */
export function fetchUpdateThirdPartyLogin(values: string[]) {
  return request.put<boolean>({
    url: '/api/settings/access-control/third-party-login',
    data: { values }
  })
}

/** 更新第三方注册渠道 */
export function fetchUpdateThirdPartyRegister(values: string[]) {
  return request.put<boolean>({
    url: '/api/settings/access-control/third-party-register',
    data: { values }
  })
}

/** 验证策略（按场景一行） */
export interface VerificationPolicyConfig {
  sceneKey: string
  enabled: boolean
  maxAttempts: number
  recoverySeconds: number
  limitByAccount: boolean
  limitByIp: boolean
}

/** 单场景可更新字段 */
export type VerificationSceneUpdate = Omit<VerificationPolicyConfig, 'sceneKey'>

/** 获取验证设置（所有场景策略） */
export function fetchGetVerificationSettings() {
  return request.get<VerificationPolicyConfig[]>({
    url: '/api/settings/verification-settings'
  })
}

/** 更新某场景的验证设置 */
export function fetchUpdateVerificationScene(sceneKey: string, data: VerificationSceneUpdate) {
  return request.put<boolean>({
    url: `/api/settings/verification-settings/${sceneKey}`,
    data
  })
}
