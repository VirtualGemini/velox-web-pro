import request from '@/utils/http'
import { AppRouteRecord } from '@/types/router'

function requireId(id: string, resourceName: string) {
  const normalizedId = id?.trim()
  if (!normalizedId) {
    throw new Error(`${resourceName}ID 不能为空`)
  }
  return normalizedId
}

// 获取用户列表
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get<Api.SystemManage.UserList>({
    url: '/api/user/list',
    params
  })
}

export function fetchCreateUser(data: Api.SystemManage.UserSaveCommand) {
  return request.post<string>({
    url: '/api/user',
    data
  })
}

export function fetchUpdateUser(userId: string, data: Api.SystemManage.UserSaveCommand) {
  const normalizedUserId = requireId(userId, '用户')
  return request.put<boolean>({
    url: `/api/user/${normalizedUserId}`,
    data
  })
}

export function fetchDeleteUser(userId: string) {
  const normalizedUserId = requireId(userId, '用户')
  return request.del<boolean>({
    url: `/api/user/${normalizedUserId}`
  })
}

// 获取角色列表
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.get<Api.SystemManage.RoleList>({
    url: '/api/role/list',
    params
  })
}

export function fetchCreateRole(data: Api.SystemManage.RoleSaveCommand) {
  return request.post<string>({
    url: '/api/role',
    data
  })
}

export function fetchUpdateRole(roleId: string, data: Api.SystemManage.RoleSaveCommand) {
  const normalizedRoleId = requireId(roleId, '角色')
  return request.put<boolean>({
    url: `/api/role/${normalizedRoleId}`,
    data
  })
}

export function fetchDeleteRole(roleId: string) {
  const normalizedRoleId = requireId(roleId, '角色')
  return request.del<boolean>({
    url: `/api/role/${normalizedRoleId}`
  })
}

// 获取角色菜单权限
export function fetchGetRoleMenuPermissions(roleId: string) {
  const normalizedRoleId = requireId(roleId, '角色')
  return request.get<string[]>({
    url: `/api/role/${normalizedRoleId}/menu-permissions`
  })
}

// 保存角色菜单权限
export function fetchUpdateRoleMenuPermissions(
  roleId: string,
  data: Api.SystemManage.RoleMenuPermissionUpdateCommand
) {
  const normalizedRoleId = requireId(roleId, '角色')
  return request.put<boolean>({
    url: `/api/role/${normalizedRoleId}/menu-permissions`,
    data
  })
}

// 获取菜单列表（导航渲染用，仅需登录）
export function fetchGetMenuList() {
  return request.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus/simple'
  })
}

// 获取可授予的菜单列表（菜单管理、角色授权弹窗用，需要 system:menu:query 权限）
// 返回数据等同于当前登录用户的可见范围——授予不能越过自己。
export function fetchGetGrantableMenus() {
  return request.get<AppRouteRecord[]>({
    url: '/api/v3/system/menus'
  })
}

export function fetchCreateMenu(data: Api.SystemManage.MenuSaveCommand) {
  return request.post<string>({
    url: '/api/v3/system/menus',
    data
  })
}

export function fetchUpdateMenu(menuId: string, data: Api.SystemManage.MenuSaveCommand) {
  const normalizedMenuId = requireId(menuId, '菜单')
  return request.put<boolean>({
    url: `/api/v3/system/menus/${normalizedMenuId}`,
    data
  })
}

export function fetchDeleteMenu(menuId: string) {
  const normalizedMenuId = requireId(menuId, '菜单')
  return request.del<boolean>({
    url: `/api/v3/system/menus/${normalizedMenuId}`
  })
}
