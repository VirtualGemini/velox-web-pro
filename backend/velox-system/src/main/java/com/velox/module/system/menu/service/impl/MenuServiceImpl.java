package com.velox.module.system.menu.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.module.system.domain.model.Menu;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.persistence.MenuMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.RoleMenuPermissionMapper;
import com.velox.module.system.persistence.support.MenuQuerySupport;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.permission.service.PermissionService;
import com.velox.module.system.menu.dto.AuthItemDTO;
import com.velox.module.system.menu.dto.MenuMetaDTO;
import com.velox.module.system.menu.dto.MenuRouteDTO;
import com.velox.module.system.menu.dto.MenuSaveCommand;
import com.velox.module.system.menu.service.MenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MenuServiceImpl implements MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);
    private static final String MENU_TYPE_BUTTON = "button";
    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;
    private final RoleMenuPermissionMapper roleMenuPermissionMapper;
    private final PermissionService permissionService;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SecuritySessionService securitySessionService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MenuServiceImpl(
            MenuMapper menuMapper,
            RoleMapper roleMapper,
            RoleMenuPermissionMapper roleMenuPermissionMapper,
            PermissionService permissionService,
            SystemEntityIdGenerator entityIdGenerator,
            SecuritySessionService securitySessionService,
            SystemFrontendIdCodecSupport frontendIdCodecSupport
    ) {
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.roleMenuPermissionMapper = roleMenuPermissionMapper;
        this.permissionService = permissionService;
        this.entityIdGenerator = entityIdGenerator;
        this.securitySessionService = securitySessionService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Override
    public List<MenuRouteDTO> getSimpleMenus() {
        return buildMenus();
    }

    @Override
    public List<MenuRouteDTO> getGrantableMenus() {
        return buildMenus();
    }

    private List<MenuRouteDTO> buildMenus() {
        String currentLoginId = securitySessionService.requireCurrentLoginId();
        Set<String> permittedMenuIds = permissionService.getAccountPermittedMenuIds(currentLoginId);

        // 调试日志
        // log.warn("=== DEBUG buildMenus ===");
        // log.warn("currentLoginId: {}", currentLoginId);
        // log.warn("permittedMenuIds size: {}", permittedMenuIds.size());
        // log.warn("permittedMenuIds: {}", permittedMenuIds);

        if (permittedMenuIds.isEmpty()) {
            log.error("WARNING: permittedMenuIds is empty, returning empty list");
            return new ArrayList<>();
        }

        // 把祖先菜单一并纳入：仅用于让路由树保持连通（譬如 /system/account-center 需要 System 父节点），
        // 不等同于授予按钮权限——按钮鉴权仍走 PermissionService 的 authMark 集合。
        Set<String> menuTreeIds = expandWithAncestors(permittedMenuIds);

        List<Menu> menus = menuMapper.selectList(
                MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                        .eq(Menu::getDeleted, 0)
                        .eq(Menu::getIsEnable, 1)
                        .in(Menu::getId, menuTreeIds)
                        .orderByAsc(Menu::getSort)
                        .orderByAsc(Menu::getCreateTime)
        );

        Map<String, List<String>> menuRoleCodeMap = getMenuRoleCodes(menus.stream()
                .map(Menu::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        Map<String, MenuRouteDTO> routeMap = new LinkedHashMap<>();
        List<Menu> buttonMenus = new ArrayList<>();

        for (Menu menu : menus) {
            if (MENU_TYPE_BUTTON.equalsIgnoreCase(menu.getMenuType())) {
                buttonMenus.add(menu);
                continue;
            }
            routeMap.put(menu.getId(), toRoute(menu, menuRoleCodeMap.get(menu.getId())));
        }

        for (Menu buttonMenu : buttonMenus) {
            MenuRouteDTO parent = routeMap.get(buttonMenu.getParentId());
            if (parent == null || parent.getMeta() == null) {
                continue;
            }
            AuthItemDTO authItemDTO = new AuthItemDTO();
            authItemDTO.setId(buttonMenu.getId());
            authItemDTO.setTitle(buttonMenu.getTitle());
            authItemDTO.setAuthMark(buttonMenu.getAuthMark());
            authItemDTO.setSort(buttonMenu.getSort());
            parent.getMeta().getAuthList().add(authItemDTO);
        }

        List<MenuRouteDTO> roots = new ArrayList<>();
        for (Menu menu : menus) {
            if (MENU_TYPE_BUTTON.equalsIgnoreCase(menu.getMenuType())) {
                continue;
            }
            MenuRouteDTO route = routeMap.get(menu.getId());
            if (route == null) {
                continue;
            }
            if (menu.getParentId() == null) {
                roots.add(route);
                continue;
            }
            MenuRouteDTO parent = routeMap.get(menu.getParentId());
            if (parent != null) {
                parent.getChildren().add(route);
            } else {
                roots.add(route);
            }
        }

        // 门禁：定义了按钮权限却一个都没授予当前用户的页面无任何可用操作，连同被掏空的目录一并隐藏，
        // 使「一个管理权限都没有 -> 该页面无路由 + 无法访问」在后端菜单构建层生效。R_SUPER 持有全部按钮，天然不受影响。
        Set<String> menusWithDefinedButtons = collectMenusWithDefinedButtons();
        List<MenuRouteDTO> visibleRoots = pruneInaccessibleRoutes(roots, menusWithDefinedButtons);

        sortRoutes(visibleRoots);

        return visibleRoots;
    }

    /**
     * 全量收集「至少定义了一个启用按钮」的菜单 ID（不区分是否授予当前用户）。
     * 用于判断某页面是否属于「按钮驱动可见性」的页面。
     */
    private Set<String> collectMenusWithDefinedButtons() {
        return menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                        .eq(Menu::getDeleted, 0)
                        .eq(Menu::getIsEnable, 1)
                        .eq(Menu::getMenuType, MENU_TYPE_BUTTON))
                .stream()
                .map(Menu::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 递归裁剪不可访问的路由节点：
     * <ol>
     *   <li>叶子页面定义了按钮权限、但当前用户一个都没拿到 -> 隐藏（无任何可用操作）；</li>
     *   <li>目录节点的子菜单被全部裁剪后变为空壳 -> 隐藏（避免空菜单组）。</li>
     * </ol>
     */
    private List<MenuRouteDTO> pruneInaccessibleRoutes(List<MenuRouteDTO> nodes, Set<String> menusWithDefinedButtons) {
        List<MenuRouteDTO> kept = new ArrayList<>();
        for (MenuRouteDTO node : nodes) {
            boolean wasDirectory = node.getChildren() != null && !node.getChildren().isEmpty();
            if (wasDirectory) {
                node.setChildren(pruneInaccessibleRoutes(node.getChildren(), menusWithDefinedButtons));
            }
            if (!shouldHideRoute(node, wasDirectory, menusWithDefinedButtons)) {
                kept.add(node);
            }
        }
        return kept;
    }

    private boolean shouldHideRoute(MenuRouteDTO node, boolean wasDirectory, Set<String> menusWithDefinedButtons) {
        boolean hasChildren = node.getChildren() != null && !node.getChildren().isEmpty();
        // 目录被掏空 -> 隐藏
        if (wasDirectory) {
            return !hasChildren;
        }
        // 叶子页面：定义了按钮却未授予任何一个 -> 隐藏
        if (menusWithDefinedButtons.contains(node.getId())) {
            return node.getMeta() == null
                    || node.getMeta().getAuthList() == null
                    || node.getMeta().getAuthList().isEmpty();
        }
        return false;
    }

    private MenuRouteDTO toRoute(Menu menu, List<String> roleCodes) {
        MenuRouteDTO route = new MenuRouteDTO();
        route.setId(menu.getId());
        route.setPath(menu.getPath());
        route.setName(menu.getName());
        route.setComponent(menu.getComponent());
        route.setRedirect(menu.getRedirect());
        route.setMeta(toMeta(menu, roleCodes));
        return route;
    }

    private MenuMetaDTO toMeta(Menu menu, List<String> roleCodes) {
        MenuMetaDTO meta = new MenuMetaDTO();
        meta.setTitle(menu.getTitle());
        meta.setIcon(menu.getIcon());
        meta.setAuthMark(menu.getAuthMark());
        meta.setIsEnable(isTrue(menu.getIsEnable()));
        meta.setSort(menu.getSort());
        if (roleCodes != null && !roleCodes.isEmpty()) {
            meta.setRoles(roleCodes);
        }
        meta.setShowBadge(isTrue(menu.getShowBadge()));
        meta.setShowTextBadge(menu.getShowTextBadge());
        meta.setIsHide(isTrue(menu.getIsHide()));
        meta.setIsHideTab(isTrue(menu.getIsHideTab()));
        meta.setLink(menu.getLink());
        meta.setIsIframe(isTrue(menu.getIsIframe()));
        meta.setKeepAlive(isTrue(menu.getKeepAlive()));
        meta.setFixedTab(isTrue(menu.getFixedTab()));
        meta.setActivePath(menu.getActivePath());
        meta.setIsFullPage(isTrue(menu.getIsFullPage()));
        meta.setCreateTime(RequestDateTimeFormatter.format(menu.getCreateTime()));
        meta.setUpdateTime(RequestDateTimeFormatter.format(menu.getUpdateTime()));
        return meta;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(MenuSaveCommand command) {
        validateMenuCommand(command, null);
        ensureAuthMarkUnique(command.getAuthMark(), null);

        Menu menu = new Menu();
        menu.setId(entityIdGenerator.nextId(Menu.class));
        applyCommand(menu, command);
        menu.setDeleted(0);
        menu.setCreateBy(currentOperator());
        menu.setUpdateBy(currentOperator());
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String menuId, MenuSaveCommand command) {
        String decodedMenuId = frontendIdCodecSupport.decodeIdentifier(menuId);
        Menu menu = getActiveMenu(decodedMenuId);
        validateMenuCommand(command, decodedMenuId);
        ensureAuthMarkUnique(command.getAuthMark(), decodedMenuId);
        applyCommand(menu, command);
        menu.setUpdateBy(currentOperator());
        menuMapper.updateById(menu);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String menuId) {
        String decodedMenuId = frontendIdCodecSupport.decodeIdentifier(menuId);
        Menu menu = getActiveMenu(decodedMenuId);

        // 检查是否有子菜单
        long childCount = menuMapper.selectCount(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getParentId, decodedMenuId)
                .eq(Menu::getDeleted, 0));

        if (childCount > 0) {
            throw new ApiException(BusinessErrorCode.MENU_HAS_CHILDREN);
        }

        String operator = currentOperator();

        // 删除角色菜单权限关联
        roleMenuPermissionMapper.delete(new LambdaQueryWrapper<com.velox.module.system.domain.model.RoleMenuPermission>()
                .eq(com.velox.module.system.domain.model.RoleMenuPermission::getMenuId, decodedMenuId));

        // 逻辑删除菜单
        menuMapper.update(null, new LambdaUpdateWrapper<Menu>()
                .eq(Menu::getId, decodedMenuId)
                .eq(Menu::getDeleted, 0)
                .set(Menu::getDeleted, 1)
                .set(Menu::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(Menu::getUpdateBy, operator));
        return true;
    }

    private boolean isTrue(Integer value) {
        return Integer.valueOf(1).equals(value);
    }

    private void sortRoutes(List<MenuRouteDTO> routes) {
        routes.sort((left, right) -> {
            Integer leftSort = left.getMeta() == null || left.getMeta().getSort() == null ? Integer.MAX_VALUE : left.getMeta().getSort();
            Integer rightSort = right.getMeta() == null || right.getMeta().getSort() == null ? Integer.MAX_VALUE : right.getMeta().getSort();
            int sortCompare = Integer.compare(leftSort, rightSort);
            if (sortCompare != 0) {
                return sortCompare;
            }
            String leftName = left.getName() == null ? "~" : left.getName();
            String rightName = right.getName() == null ? "~" : right.getName();
            return leftName.compareTo(rightName);
        });
        for (MenuRouteDTO route : routes) {
            if (route.getChildren() != null && !route.getChildren().isEmpty()) {
                sortRoutes(route.getChildren());
            }
        }
    }



    private void applyCommand(Menu menu, MenuSaveCommand command) {
        boolean isButton = MENU_TYPE_BUTTON.equalsIgnoreCase(command.getMenuType());
        menu.setParentId(isButton ? normalizeParentId(command.getParentId()) : normalizeMenuParentId(command.getParentId()));
        menu.setMenuType(command.getMenuType().trim().toLowerCase());
        menu.setName(command.getName().trim());
        menu.setTitle(command.getTitle().trim());
        menu.setPath(isButton ? null : normalizeNullable(command.getPath()));
        menu.setComponent(isButton ? null : normalizeNullable(command.getComponent()));
        menu.setRedirect(isButton ? null : normalizeNullable(command.getRedirect()));
        menu.setIcon(isButton ? null : normalizeNullable(command.getIcon()));
        menu.setAuthMark(normalizeNullable(command.getAuthMark()));
        menu.setIsEnable(Boolean.TRUE.equals(command.getIsEnable()) ? 1 : 0);
        menu.setSort(command.getSort() == null ? 1 : command.getSort());
        menu.setKeepAlive(Boolean.TRUE.equals(command.getKeepAlive()) ? 1 : 0);
        menu.setIsHide(Boolean.TRUE.equals(command.getIsHide()) ? 1 : 0);
        menu.setIsHideTab(Boolean.TRUE.equals(command.getIsHideTab()) ? 1 : 0);
        menu.setLink(normalizeNullable(command.getLink()));
        menu.setIsIframe(Boolean.TRUE.equals(command.getIsIframe()) ? 1 : 0);
        menu.setShowBadge(Boolean.TRUE.equals(command.getShowBadge()) ? 1 : 0);
        menu.setShowTextBadge(normalizeNullable(command.getShowTextBadge()));
        menu.setFixedTab(Boolean.TRUE.equals(command.getFixedTab()) ? 1 : 0);
        menu.setActivePath(normalizeNullable(command.getActivePath()));
        menu.setIsFullPage(Boolean.TRUE.equals(command.getIsFullPage()) ? 1 : 0);
    }

    private void validateMenuCommand(MenuSaveCommand command, String menuId) {
        String menuType = command.getMenuType() == null ? "" : command.getMenuType().trim().toLowerCase();
        if (!"menu".equals(menuType) && !MENU_TYPE_BUTTON.equals(menuType)) {
            throw new ApiException(BusinessErrorCode.MENU_TYPE_INVALID);
        }

        if (MENU_TYPE_BUTTON.equals(menuType)) {
            String parentId = normalizeParentId(command.getParentId());
            Menu parentMenu = getActiveMenu(parentId);
            if (MENU_TYPE_BUTTON.equalsIgnoreCase(parentMenu.getMenuType())) {
                throw new ApiException(BusinessErrorCode.MENU_BUTTON_PARENT_INVALID);
            }
        }

        if ("menu".equals(menuType) && !StringUtils.hasText(command.getPath())) {
            throw new ApiException(BusinessErrorCode.MENU_PATH_REQUIRED);
        }

        // 权限标识为可选字段，不做非空校验

        ensureNameUnique(command.getName(), menuId);
    }

    private void ensureNameUnique(String name, String excludeMenuId) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<Menu>()
                .eq(Menu::getDeleted, 0)
                .eq(Menu::getName, name == null ? null : name.trim());
        if (excludeMenuId != null) {
            wrapper.ne(Menu::getId, excludeMenuId);
        }
        if (menuMapper.selectCount(wrapper) > 0) {
            throw new ApiException(BusinessErrorCode.DATA_ALREADY_EXISTS);
        }
    }

    private void ensureAuthMarkUnique(String authMark, String excludeMenuId) {
        if (!StringUtils.hasText(authMark)) {
            return;
        }
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<Menu>()
                .eq(Menu::getDeleted, 0)
                .eq(Menu::getAuthMark, authMark.trim());
        if (excludeMenuId != null) {
            wrapper.ne(Menu::getId, excludeMenuId);
        }
        if (menuMapper.selectCount(wrapper) > 0) {
            throw new ApiException(BusinessErrorCode.DATA_ALREADY_EXISTS);
        }
    }

    private Menu getActiveMenu(String menuId) {
        Menu menu = menuMapper.selectOne(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                .eq(Menu::getId, menuId)
                .eq(Menu::getDeleted, 0)
                .last("limit 1"));
        if (menu == null) {
            throw new ApiException(BusinessErrorCode.DATA_NOT_FOUND);
        }
        return menu;
    }

    /**
     * 把入参菜单 ID 集合扩展为「自身 + 所有启用的祖先菜单」。
     * 用于让前端拿到的菜单树保持连通：仅授权了 UserCenter（其 parent 为 System）时，
     * System 节点必须出现在返回结果中，否则前端拼接出来的路径会变成 /account-center 而非 /system/account-center。
     */
    private Set<String> expandWithAncestors(Set<String> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Set.of();
        }
        Set<String> expanded = new LinkedHashSet<>(menuIds);
        Set<String> frontier = new LinkedHashSet<>(menuIds);
        while (!frontier.isEmpty()) {
            List<Menu> menus = menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                    .eq(Menu::getDeleted, 0)
                    .eq(Menu::getIsEnable, 1)
                    .in(Menu::getId, frontier));
            Set<String> nextFrontier = new LinkedHashSet<>();
            for (Menu menu : menus) {
                String parentId = menu.getParentId();
                if (parentId == null || expanded.contains(parentId)) {
                    continue;
                }
                expanded.add(parentId);
                nextFrontier.add(parentId);
            }
            frontier = nextFrontier;
        }
        return expanded;
    }

    private Map<String, List<String>> getMenuRoleCodes(Collection<String> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Map.of();
        }

        Map<String, Set<String>> menuRoleIdMap = new LinkedHashMap<>();
        roleMenuPermissionMapper.selectList(new LambdaQueryWrapper<com.velox.module.system.domain.model.RoleMenuPermission>()
                        .eq(com.velox.module.system.domain.model.RoleMenuPermission::getDeleted, 0)
                        .in(com.velox.module.system.domain.model.RoleMenuPermission::getMenuId, menuIds))
                .forEach(item -> menuRoleIdMap
                        .computeIfAbsent(item.getMenuId(), key -> new LinkedHashSet<>())
                        .add(item.getRoleId()));

        Set<String> roleIds = menuRoleIdMap.values().stream()
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return Map.of();
        }

        Map<String, String> roleCodeMap = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                        .eq(Role::getDeleted, 0)
                        .in(Role::getId, roleIds))
                .stream()
                .filter(role -> StringUtils.hasText(role.getRoleCode()))
                .collect(Collectors.toMap(Role::getId, Role::getRoleCode, (left, right) -> left, LinkedHashMap::new));

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : menuRoleIdMap.entrySet()) {
            List<String> roleCodes = entry.getValue().stream()
                    .map(roleCodeMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!roleCodes.isEmpty()) {
                result.put(entry.getKey(), roleCodes);
            }
        }
        return result;
    }

    private String normalizeParentId(String parentId) {
        if (!StringUtils.hasText(parentId)) {
            throw new ApiException(BusinessErrorCode.MENU_PARENT_REQUIRED);
        }
        return parentId.trim();
    }

    private String normalizeMenuParentId(String parentId) {
        return StringUtils.hasText(parentId) ? parentId.trim() : null;
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return StringUtils.hasText(loginId) ? loginId : "system";
    }
}
