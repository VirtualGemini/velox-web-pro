package com.velox.module.system.auth.status;

import java.util.Collection;
import java.util.Map;

public interface ActiveUserStatusService {

    String STATUS_ONLINE = "1";
    String STATUS_OFFLINE = "2";

    void recordRequestActivity(String userId);

    void recordLogin(String userId);

    void recordLogout(String userId);

    boolean isOnline(String userId);

    Map<String, String> resolveStatuses(Collection<String> userIds);
}
