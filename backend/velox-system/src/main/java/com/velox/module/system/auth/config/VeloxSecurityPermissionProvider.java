package com.velox.module.system.auth.config;

import com.velox.framework.security.api.authorization.SecurityPermissionProvider;
import com.velox.module.system.permission.service.PermissionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VeloxSecurityPermissionProvider implements SecurityPermissionProvider {

    private final PermissionService permissionService;

    public VeloxSecurityPermissionProvider(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public List<String> getPermissions(String loginId) {
        String accountId = normalizeLoginId(loginId);
        if (accountId == null) {
            return List.of();
        }
        return permissionService.getAccountPermissionMarks(accountId);
    }

    @Override
    public List<String> getRoles(String loginId) {
        String accountId = normalizeLoginId(loginId);
        if (accountId == null) {
            return List.of();
        }
        return permissionService.getAccountRoleCodes(accountId);
    }

    private String normalizeLoginId(String loginId) {
        if (loginId == null) {
            return null;
        }
        String normalized = loginId.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
