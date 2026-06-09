package com.velox.framework.security.api.authorization;

import java.util.Collection;

public interface SecurityAuthorizationService {

    void checkAuthenticated();

    void checkPermission(String permission);

    /**
     * 校验当前用户是否至少持有候选权限中的任意一个，全部缺失时拒绝。
     */
    void checkAnyPermission(Collection<String> permissions);
}
