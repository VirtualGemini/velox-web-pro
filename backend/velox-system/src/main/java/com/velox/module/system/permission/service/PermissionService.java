package com.velox.module.system.permission.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PermissionService {

    Set<String> getRoleMenuIds(String roleId);

    Set<String> getRoleMenuIds(Collection<String> roleIds);

    void assignRoleMenu(String roleId, Collection<String> menuIds);

    Set<String> getAccountRoleIds(String accountId);

    Map<String, Set<String>> getAccountRoleIds(Collection<String> accountIds);

    List<String> getAccountRoleCodes(String accountId);

    Map<String, List<String>> getAccountRoleCodes(Collection<String> accountIds);

    Integer getAccountHighestRoleLevel(String accountId);

    Map<String, Integer> getAccountHighestRoleLevels(Collection<String> accountIds);

    List<String> getAccountPermissionMarks(String accountId);

    /**
     * 获取指定用户可见的菜单 ID 集合（启用、未删除）。
     * R_SUPER 直通所有启用菜单；普通用户为其角色累积的菜单 ID。
     * 用于：导航菜单渲染、角色授权范围校验等"个人可见范围"场景。
     */
    Set<String> getAccountPermittedMenuIds(String accountId);
}
