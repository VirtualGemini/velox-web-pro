package com.velox.module.system.auth.config;

import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.auth.session.AccountSessionService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.auth.store.RedisVerificationCodeStore;
import com.velox.module.system.auth.store.VerificationCodeStore;
import com.velox.module.system.verification.service.VerificationPolicyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AuthStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean(ActiveUserStatusService.class)
    public ActiveUserStatusService activeUserStatusService(AccountSessionService accountSessionService) {
        return new ActiveUserStatusService() {
            @Override
            public void recordRequestActivity(String userId, String tokenValue) {
                accountSessionService.recordRequestActivity(userId, tokenValue);
            }

            @Override
            public void recordLogin(String userId, String sessionId, String tokenValue) {
                accountSessionService.recordLogin(userId, sessionId, tokenValue);
            }

            @Override
            public void recordLogout(String userId, String tokenValue) {
                accountSessionService.recordLogout(userId, tokenValue);
            }

            @Override
            public String sessionIdByToken(String tokenValue) {
                return accountSessionService.sessionIdByToken(tokenValue);
            }

            @Override
            public boolean isOnline(String userId) {
                return accountSessionService.isOnline(userId);
            }

            @Override
            public java.util.Map<String, String> resolveStatuses(java.util.Collection<String> userIds) {
                return accountSessionService.resolveStatuses(userIds);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(VerificationCodeStore.class)
    public VerificationCodeStore redisVerificationCodeStore(StringRedisTemplate stringRedisTemplate,
                                                            SystemAuthProperties authProperties,
                                                            VerificationPolicyService policyService) {
        return new RedisVerificationCodeStore(stringRedisTemplate, authProperties, policyService);
    }
}
