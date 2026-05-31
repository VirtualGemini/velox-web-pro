package com.velox.module.system.menu.service.impl;

import com.velox.module.system.common.constants.SystemRoleCode;
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
        return buildMenus(false);
    }

    @Override
    public List<MenuRouteDTO> getGrantableMenus() {
        return buildMenus(true);
    }

    private List<MenuRouteDTO> buildMenus(boolean preservePermissionGrouping) {
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

        // 页面级闸门（仅作用于导航/路由下发，不影响角色授权树）：
        // 对定义了 *:view 按钮的页面，若当前用户未获得该 view 权限，
        // 则整页不下发（导航不显示、路由不注册，强行输入 URL 由前端路由守卫拦回首页）。
        // 依据：route 的 authList 已是「已授权」按钮集合（超管拥有全部，自然不被过滤）。

        // 临时禁用页面级闸门进行调试
        if (!preservePermissionGrouping) {
            Map<String, String> pageViewMarks = loadPageViewMarks();
            if (!pageViewMarks.isEmpty()) {
                Set<String> hiddenRouteIds = new LinkedHashSet<>();
                for (MenuRouteDTO route : routeMap.values()) {
                    String viewMark = pageViewMarks.get(route.getId());
                    if (viewMark == null) {
                        continue;
                    }
                    // 只对「真实可导航页面」生效（有 component）；隐藏的权限分组节点
                    // （如账号中心，无 path/component，其按钮在 flatten 阶段上提父级）不在此过滤。
                    if (!StringUtils.hasText(route.getComponent())) {
                        continue;
                    }
                    boolean hasView = route.getMeta() != null
                            && route.getMeta().getAuthList().stream()
                            .anyMatch(item -> viewMark.equals(item.getAuthMark()));
                    if (!hasView) {
                        hiddenRouteIds.add(route.getId());
                    }
                }
                hiddenRouteIds.forEach(routeMap::remove);
            }
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

        sortRoutes(roots);

        // log.warn("Before pruneEmptyDirectories: roots size = {}", roots.size());

        // 临时禁用 pruneEmptyDirectories，因为页面级闸门已被禁用
        /*
        if (!preservePermissionGrouping) {
            // 页面闸门可能把某个父目录的全部数据页过滤掉，导致空目录残留；按 DB 中的父子关系剪枝。
            Set<String> directoryIds = menus.stream()
                    .map(Menu::getParentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            log.warn("directoryIds size: {}", directoryIds.size());
            log.warn("directoryIds: {}", directoryIds);

            roots = pruneEmptyDirectories(roots, directoryIds);

            log.warn("After pruneEmptyDirectories: roots size = {}", roots.size());
        }
        */

        List<MenuRouteDTO> result = preservePermissionGrouping ? roots : flattenPermissionGroupingMenus(roots);

        // 调试日志：查看最终返回结果
        // log.warn("=== Final menu result ===");
        // log.warn("Result size: {}", result.size());
        // if (!result.isEmpty()) {
        //     log.warn("First menu: id={}, name={}, path={}",
        //         result.get(0).getId(),
        //         result.get(0).getName(),
        //         result.get(0).getPath());
        // }

        return result;
    }

    /**
     * 加载页面访问权限标识：DB 中所有以 {@code :view} 结尾的按钮，映射「所属页面菜单ID → authMark」。
     * 用于页面级闸门——与当前用户的授权无关，仅声明「哪些页面需要 view 权限才能访问」。
     */
    private Map<String, String> loadPageViewMarks() {
        List<Menu> viewButtons = menuMapper.selectList(
                MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                        .eq(Menu::getDeleted, 0)
                        .eq(Menu::getIsEnable, 1)
                        .eq(Menu::getMenuType, MENU_TYPE_BUTTON)
                        .isNotNull(Menu::getAuthMark)
        );
        Map<String, String> result = new LinkedHashMap<>();
        for (Menu button : viewButtons) {
            String authMark = button.getAuthMark();
            String parentId = button.getParentId();
            if (authMark != null && authMark.endsWith(":view") && parentId != null) {
                result.put(parentId, authMark);
            }
        }
        return result;
    }

    /**
     * 自底向上剪掉「空目录」：原本是父目录（在 DB 中作为他人 parentId 出现）但过滤后已无任何子路由的节点。
     * 叶子页面（非目录）即使无子节点也会保留。
     */
    private List<MenuRouteDTO> pruneEmptyDirectories(List<MenuRouteDTO> routes, Set<String> directoryIds) {
        List<MenuRouteDTO> kept = new ArrayList<>();
        for (MenuRouteDTO route : routes) {
            if (route.getChildren() != null && !route.getChildren().isEmpty()) {
                route.setChildren(pruneEmptyDirectories(route.getChildren(), directoryIds));
            }
            boolean isDirectory = directoryIds.contains(route.getId());
            boolean hasChildren = route.getChildren() != null && !route.getChildren().isEmpty();
            if (isDirectory && !hasChildren) {
                continue;
            }
            kept.add(route);
        }
        return kept;
    }

    private List<MenuRouteDTO> flattenPermissionGroupingMenus(List<MenuRouteDTO> routes) {
        List<MenuRouteDTO> flattened = new ArrayList<>();
        for (MenuRouteDTO route : routes) {
            flattenPermissionGroupingRoute(route);
            flattened.add(route);
        }
        return flattened;
    }

    private void flattenPermissionGroupingRoute(MenuRouteDTO route) {
        if (route.getChildren() == null || route.getChildren().isEmpty()) {
            return;
        }

        List<MenuRouteDTO> normalizedChildren = new ArrayList<>();
        for (MenuRouteDTO child : route.getChildren()) {
            if (isAccountCenterPermissionGroup(child)) {
                if (route.getMeta() != null
                        && child.getMeta() != null
                        && child.getMeta().getAuthList() != null
                        && !child.getMeta().getAuthList().isEmpty()) {
                    route.getMeta().getAuthList().addAll(child.getMeta().getAuthList());
                }
                continue;
            }
            flattenPermissionGroupingRoute(child);
            normalizedChildren.add(child);
        }
        route.setChildren(normalizedChildren);
    }

    private boolean isAccountCenterPermissionGroup(MenuRouteDTO route) {
        if (route == null) {
            return false;
        }
        if (route.getMeta() == null) {
            return false;
        }
        return Boolean.TRUE.equals(route.getMeta().getIsHide())
                && !StringUtils.hasText(route.getPath())
                && !StringUtils.hasText(route.getComponent());
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
        bindMenuToSuperRole(menu.getId());
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

    private Set<String> collectDescendantIds(Collection<String> rootIds) {
        Set<String> collected = new LinkedHashSet<>(rootIds);
        Set<String> frontier = new LinkedHashSet<>(rootIds);
        while (!frontier.isEmpty()) {
            List<Menu> children = menuMapper.selectList(MenuQuerySupport.selectColumns(new LambdaQueryWrapper<Menu>())
                    .eq(Menu::getDeleted, 0)
                    .in(Menu::getParentId, frontier));
            frontier = children.stream()
                    .map(Menu::getId)
                    .filter(Objects::nonNull)
                    .filter(id -> !collected.contains(id))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            collected.addAll(frontier);
        }
        return collected;
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

    private void bindMenuToSuperRole(String menuId) {
        if (!StringUtils.hasText(menuId)) {
            return;
        }
        Role superRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .eq(Role::getRoleCode, SystemRoleCode.R_SUPER)
                .last("limit 1"));
        if (superRole == null || !StringUtils.hasText(superRole.getId())) {
            return;
        }
        LinkedHashSet<String> menuIds = new LinkedHashSet<>(permissionService.getRoleMenuIds(superRole.getId()));
        menuIds.add(menuId);
        permissionService.assignRoleMenu(superRole.getId(), menuIds);
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return StringUtils.hasText(loginId) ? loginId : "system";
    }
}
