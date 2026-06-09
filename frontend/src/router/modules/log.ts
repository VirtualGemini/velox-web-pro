import { AppRouteRecord } from '@/types/router'

export const logRoutes: AppRouteRecord = {
  path: '/log',
  name: 'LogManagement',
  component: '/index/index',
  meta: {
    title: 'menus.log.title',
    icon: 'ri:file-list-3-line',
    roles: ['R_SUPER', 'R_ADMIN']
  },
  children: [
    {
      path: 'login',
      name: 'LoginLog',
      component: '/system/log/login',
      meta: {
        title: 'menus.log.login',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN'],
        authList: [
          { title: 'pages.system.log.common.auth.query', authMark: 'system:log:login:query' },
          { title: 'pages.system.log.common.auth.delete', authMark: 'system:log:login:delete' },
          { title: 'pages.system.log.common.auth.clean', authMark: 'system:log:login:clean' }
        ]
      }
    },
    {
      path: 'operation',
      name: 'OperationLog',
      component: '/system/log/operation',
      meta: {
        title: 'menus.log.operation',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN'],
        authList: [
          { title: 'pages.system.log.common.auth.query', authMark: 'system:log:operation:query' },
          { title: 'pages.system.log.common.auth.delete', authMark: 'system:log:operation:delete' },
          { title: 'pages.system.log.common.auth.clean', authMark: 'system:log:operation:clean' }
        ]
      }
    },
    {
      path: 'api',
      name: 'ApiLog',
      component: '/system/log/api',
      meta: {
        title: 'menus.log.api',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN'],
        authList: [
          { title: 'pages.system.log.common.auth.query', authMark: 'system:log:api:query' },
          { title: 'pages.system.log.common.auth.delete', authMark: 'system:log:api:delete' },
          { title: 'pages.system.log.common.auth.clean', authMark: 'system:log:api:clean' }
        ]
      }
    }
  ]
}
