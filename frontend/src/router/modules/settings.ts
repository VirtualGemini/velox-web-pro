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
          { title: '查询', authMark: 'settings:access-control:query' },
          { title: '通用注册管理', authMark: 'settings:access-control:general-register' },
          { title: '第三方注册管理', authMark: 'settings:access-control:third-party-register' },
          { title: '普通登录方式管理', authMark: 'settings:access-control:login-method' },
          { title: '第三方登录方式管理', authMark: 'settings:access-control:third-party-login' },
          { title: '忘记密码管理', authMark: 'settings:access-control:forgot-password' }
        ]
      }
    }
  ]
}
