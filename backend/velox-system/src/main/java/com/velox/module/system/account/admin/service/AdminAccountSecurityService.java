package com.velox.module.system.account.admin.service;

import com.velox.module.system.account.dto.AdminLoginMethodsCommand;
import com.velox.module.system.account.dto.AdminMfaEmailCommand;
import com.velox.module.system.account.dto.AdminOauthChannelCommand;
import com.velox.module.system.account.dto.AdminPasswordResetCommand;
import com.velox.module.system.account.dto.AdminSecurityEmailCommand;

/**
 * 管理员对他人账号安全 / 第三方渠道的管理（无需被管理用户的二次验证）。
 */
public interface AdminAccountSecurityService {

    Boolean resetPassword(String accountId, AdminPasswordResetCommand command);

    Boolean updateSecurityEmail(String accountId, AdminSecurityEmailCommand command);

    Boolean updateMfaEmail(String accountId, AdminMfaEmailCommand command);

    Boolean disableTotp(String accountId);

    Boolean updateLoginMethods(String accountId, AdminLoginMethodsCommand command);

    Boolean toggleOauthChannel(String accountId, String channel, AdminOauthChannelCommand command);

    Boolean unbindOauthChannel(String accountId, String channel);
}
