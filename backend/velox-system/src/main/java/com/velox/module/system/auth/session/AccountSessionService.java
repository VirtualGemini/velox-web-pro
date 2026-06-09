package com.velox.module.system.auth.session;

import java.util.Collection;
import java.util.Map;

public interface AccountSessionService {

    String STATUS_ONLINE = "1";
    String STATUS_OFFLINE = "2";

    void recordLogin(String accountId, String sessionId, String tokenValue);

    void recordRequestActivity(String accountId, String tokenValue);

    void recordLogout(String accountId, String tokenValue);

    void forceLogoutAll(String accountId);

    String sessionIdByToken(String tokenValue);

    boolean isOnline(String accountId);

    Map<String, String> resolveStatuses(Collection<String> accountIds);
}
