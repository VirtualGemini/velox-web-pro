import { AppRouteRecord } from '@/types/router'

export const configRoutes: AppRouteRecord = {
  path: '/config',
  name: 'Config',
  component: '/index/index',
  meta: {
    title: 'menus.config.title',
    icon: 'ri:settings-3-line',
    roles: ['R_SUPER']
  },
  children: [
    {
      path: 'file-config',
      name: 'FileConfig',
      component: '/config/file-config',
      meta: {
        title: 'menus.config.fileConfig',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '查询', authMark: 'system:file-config:query' },
          { title: '新增', authMark: 'system:file-config:create' },
          { title: '编辑', authMark: 'system:file-config:update' },
          { title: '删除', authMark: 'system:file-config:delete' }
        ]
      }
    }
  ]
}
