import { AppRouteRecord } from '@/types/router'

export const systemRoutes: AppRouteRecord = {
  path: '/system',
  name: 'System',
  component: '/index/index',
  meta: {
    title: 'menus.system.title',
    icon: 'ri:user-3-line',
    roles: ['R_SUPER', 'R_ADMIN']
  },
  children: [
    {
      path: 'user',
      name: 'User',
      component: '/system/user',
      meta: {
        title: 'menus.system.user',
        keepAlive: true,
        roles: ['R_SUPER', 'R_ADMIN']
      }
    },
    {
      path: 'role',
      name: 'Role',
      component: '/system/role',
      meta: {
        title: 'menus.system.role',
        keepAlive: true,
        roles: ['R_SUPER']
      }
    },
    {
      path: 'user-center',
      name: 'UserCenter',
      component: '/system/user-center',
      meta: {
        title: 'menus.system.userCenter',
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
        title: 'menus.system.menu',
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
        title: 'menus.system.fileManage',
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
