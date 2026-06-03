import { AppRouteRecord } from '@/types/router'

export const systemRoutes: AppRouteRecord = {
  path: '/system',
  name: 'System',
  component: '/index/index',
  meta: {
    title: '系统管理',
    icon: 'ri:user-3-line',
    roles: ['R_SUPER', 'R_ADMIN']
  },
  children: [
    {
      path: 'account',
      name: 'Account',
      component: '/system/account',
      meta: {
        title: '账号管理',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'role',
      name: 'Role',
      component: '/system/role',
      meta: {
        title: '角色管理',
        keepAlive: true,
        roles: ['R_SUPER']
      }
    },
    {
      path: 'account-center',
      name: 'AccountCenter',
      component: '/system/account-center',
      meta: {
        title: '账号中心',
        isHide: true,
        keepAlive: true,
        isHideTab: true
      }
    },
    {
      path: 'menu',
      name: 'Menus',
      component: '/system/menu',
      meta: {
        title: '菜单管理',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '新增', authMark: 'system:menu:create' },
          { title: '编辑', authMark: 'system:menu:update' },
          { title: '删除', authMark: 'system:menu:delete' }
        ]
      }
    },
    {
      path: 'file-manage',
      name: 'FileManage',
      component: '/system/file-manage',
      meta: {
        title: '文件管理',
        keepAlive: true,
        roles: ['R_SUPER'],
        authList: [
          { title: '查询', authMark: 'system:file:query' },
          { title: '新增', authMark: 'system:file:create' },
          { title: '编辑', authMark: 'system:file:update' },
          { title: '删除', authMark: 'system:file:delete' },
          { title: '下载', authMark: 'system:file:download' },
          { title: '上传', authMark: 'system:file:upload' }
        ]
      }
    }
  ]
}
