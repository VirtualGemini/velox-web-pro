import { AppRouteRecord } from '@/types/router'

export const configRoutes: AppRouteRecord = {
  path: '/config',
  name: 'Config',
  component: '/index/index',
  meta: {
    title: '配置管理',
    icon: 'ri:settings-3-line',
    roles: ['R_SUPER']
  },
  children: [
    {
      path: 'file-config',
      name: 'FileConfig',
      component: '/config/file-config',
      meta: {
        title: '文件配置',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '查询', authMark: 'system:file-config:query' },
          { title: '新增', authMark: 'system:file-config:create' },
          { title: '编辑', authMark: 'system:file-config:update' },
          { title: '删除', authMark: 'system:file-config:delete' }
        ]
      }
    },
    {
      path: 'mail-account',
      name: 'MailAccount',
      component: '/config/mail-account',
      meta: {
        title: '邮件配置',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '查询', authMark: 'system:mail-account:query' },
          { title: '新增', authMark: 'system:mail-account:create' },
          { title: '编辑', authMark: 'system:mail-account:update' },
          { title: '删除', authMark: 'system:mail-account:delete' },
          { title: '分组管理', authMark: 'system:mail-account:group' },
          { title: '发件渠道', authMark: 'system:mail-account:channel' }
        ]
      }
    },
    {
      path: 'mail-template',
      name: 'EmailTemplate',
      component: '/config/mail-template',
      meta: {
        title: '邮件模板',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '查询', authMark: 'system:mail-template:query' },
          { title: '新增', authMark: 'system:mail-template:create' },
          { title: '编辑', authMark: 'system:mail-template:update' },
          { title: '删除', authMark: 'system:mail-template:delete' }
        ]
      }
    }
  ]
}
