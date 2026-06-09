import { AppRouteRecord } from '@/types/router'

export const settingsRoutes: AppRouteRecord = {
  path: '/settings',
  name: 'Settings',
  component: '/index/index',
  meta: {
    title: '设置',
    icon: 'ri:settings-4-line',
    roles: ['R_SUPER']
  },
  children: [
    {
      path: 'access-control',
      name: 'AccessControl',
      component: '/settings/access-control',
      meta: {
        title: '访问控制',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '通用注册管理', authMark: 'settings:access-control:general-register' },
          { title: '第三方注册管理', authMark: 'settings:access-control:third-party-register' },
          { title: '普通登录方式管理', authMark: 'settings:access-control:login-method' },
          { title: '第三方登录方式管理', authMark: 'settings:access-control:third-party-login' },
          { title: '忘记密码管理', authMark: 'settings:access-control:forgot-password' }
        ]
      }
    },
    {
      path: 'verification-settings',
      name: 'VerificationSettings',
      component: '/settings/verification-settings',
      meta: {
        title: '验证设置',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '登陆失败锁定管理', authMark: 'settings:verification-settings:login' },
          { title: '验证码错误管理', authMark: 'settings:verification-settings:verify-code' },
          { title: '接口限流管理', authMark: 'settings:verification-settings:captcha' },
          { title: '发码频率管理', authMark: 'settings:verification-settings:send-code' },
          { title: 'MFA 验证失败管理', authMark: 'settings:verification-settings:mfa' }
        ]
      }
    }
  ]
}
