package com.velox.module.system.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "velox.system.auth")
public class SystemAuthProperties {

    private final Login login = new Login();
    private final Captcha captcha = new Captcha();
    private final Verification verification = new Verification();
    private final Interceptor interceptor = new Interceptor();
    private final RateLimit rateLimit = new RateLimit();

    public Login getLogin() {
        return login;
    }

    public Captcha getCaptcha() {
        return captcha;
    }

    public Verification getVerification() {
        return verification;
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public static class Login {

        private int maxFailCount = 5;
        private int lockMinutes = 30;
        private final Presence presence = new Presence();

        public int getMaxFailCount() {
            return maxFailCount;
        }

        public void setMaxFailCount(int maxFailCount) {
            this.maxFailCount = maxFailCount;
        }

        public int getLockMinutes() {
            return lockMinutes;
        }

        public void setLockMinutes(int lockMinutes) {
            this.lockMinutes = lockMinutes;
        }

        public Presence getPresence() {
            return presence;
        }

        public static class Presence {

            private boolean requestHeartbeatEnabled = true;
            private boolean loginSignalEnabled = true;
            private boolean logoutSignalEnabled = true;
            private int idleOfflineSeconds = 6000;
            private int logoutOfflineSeconds = 30;

            public boolean isRequestHeartbeatEnabled() {
                return requestHeartbeatEnabled;
            }

            public void setRequestHeartbeatEnabled(boolean requestHeartbeatEnabled) {
                this.requestHeartbeatEnabled = requestHeartbeatEnabled;
            }

            public boolean isLoginSignalEnabled() {
                return loginSignalEnabled;
            }

            public void setLoginSignalEnabled(boolean loginSignalEnabled) {
                this.loginSignalEnabled = loginSignalEnabled;
            }

            public boolean isLogoutSignalEnabled() {
                return logoutSignalEnabled;
            }

            public void setLogoutSignalEnabled(boolean logoutSignalEnabled) {
                this.logoutSignalEnabled = logoutSignalEnabled;
            }

            public int getIdleOfflineSeconds() {
                return idleOfflineSeconds;
            }

            public void setIdleOfflineSeconds(int idleOfflineSeconds) {
                this.idleOfflineSeconds = idleOfflineSeconds;
            }

            public int getLogoutOfflineSeconds() {
                return logoutOfflineSeconds;
            }

            public void setLogoutOfflineSeconds(int logoutOfflineSeconds) {
                this.logoutOfflineSeconds = logoutOfflineSeconds;
            }
        }
    }

    public static class Captcha {

        private int ttlSeconds = 120;
        private int maxCacheSize = 10000;
        /** 是否强制登录 captcha 票据校验；灰度/应急时可关（关时 login() 跳过票据校验）。 */
        private boolean enforce = false;
        /** 登录 captcha 票据有效期（秒）。 */
        private int ticketTtlSeconds = 180;

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public int getMaxCacheSize() {
            return maxCacheSize;
        }

        public void setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }

        public boolean isEnforce() {
            return enforce;
        }

        public void setEnforce(boolean enforce) {
            this.enforce = enforce;
        }

        public int getTicketTtlSeconds() {
            return ticketTtlSeconds;
        }

        public void setTicketTtlSeconds(int ticketTtlSeconds) {
            this.ticketTtlSeconds = ticketTtlSeconds;
        }
    }

    public static class Verification {

        private String secret = "change-this-verification-secret";
        private int resetCodeTtlSeconds = 600;
        private int resetCodeResendIntervalSeconds = 60;
        /** 同一验证码允许的最大错误次数，达到即作废该码（需重新获取）。 */
        private int maxVerifyAttempts = 5;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getResetCodeTtlSeconds() {
            return resetCodeTtlSeconds;
        }

        public void setResetCodeTtlSeconds(int resetCodeTtlSeconds) {
            this.resetCodeTtlSeconds = resetCodeTtlSeconds;
        }

        public int getResetCodeResendIntervalSeconds() {
            return resetCodeResendIntervalSeconds;
        }

        public void setResetCodeResendIntervalSeconds(int resetCodeResendIntervalSeconds) {
            this.resetCodeResendIntervalSeconds = resetCodeResendIntervalSeconds;
        }

        public int getMaxVerifyAttempts() {
            return maxVerifyAttempts;
        }

        public void setMaxVerifyAttempts(int maxVerifyAttempts) {
            this.maxVerifyAttempts = maxVerifyAttempts;
        }
    }

    public static class Interceptor {

        private List<String> authExcludeSuffixes = new ArrayList<>(List.of(
                "/login",
                "/login-code/send",
                "/login-code/login",
                "/captcha",
                "/captcha/ticket",
                "/access-config",
                "/register",
                "/forgot-password/code",
                "/forgot-password/reset",
                "/mfa/challenge/send-code",
                "/mfa/challenge/verify"
        ));

        private List<String> publicExcludePatterns = new ArrayList<>(List.of(
                "/file/*/get/**"
        ));

        private List<String> swaggerPublicExcludePatterns = new ArrayList<>(List.of(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/doc.html",
                "/webjars/**"
        ));

        public List<String> getAuthExcludeSuffixes() {
            return authExcludeSuffixes;
        }

        public void setAuthExcludeSuffixes(List<String> authExcludeSuffixes) {
            this.authExcludeSuffixes = authExcludeSuffixes == null ? new ArrayList<>() : new ArrayList<>(authExcludeSuffixes);
        }

        public List<String> getPublicExcludePatterns() {
            return publicExcludePatterns;
        }

        public void setPublicExcludePatterns(List<String> publicExcludePatterns) {
            this.publicExcludePatterns = publicExcludePatterns == null ? new ArrayList<>() : new ArrayList<>(publicExcludePatterns);
        }

        public List<String> getSwaggerPublicExcludePatterns() {
            return swaggerPublicExcludePatterns;
        }

        public void setSwaggerPublicExcludePatterns(List<String> swaggerPublicExcludePatterns) {
            this.swaggerPublicExcludePatterns = swaggerPublicExcludePatterns == null
                    ? new ArrayList<>()
                    : new ArrayList<>(swaggerPublicExcludePatterns);
        }
    }

    public static class RateLimit {

        /** 限流总开关；应急时可关（关后所有认证端点不限流）。 */
        private boolean enabled = true;
        private final TrustedProxy trustedProxy = new TrustedProxy();
        /** 认证端点限流规则：key 为 /auth 之后的后缀（如 "/login"），仅命中的端点受限。 */
        private Map<String, Rule> rules = defaultRules();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public TrustedProxy getTrustedProxy() {
            return trustedProxy;
        }

        public Map<String, Rule> getRules() {
            return rules;
        }

        public void setRules(Map<String, Rule> rules) {
            this.rules = rules == null ? new LinkedHashMap<>() : new LinkedHashMap<>(rules);
        }

        private static Map<String, Rule> defaultRules() {
            Map<String, Rule> map = new LinkedHashMap<>();
            map.put("/login", new Rule(10, 60));
            map.put("/captcha/ticket", new Rule(10, 60));
            map.put("/login-code/send", new Rule(5, 60));
            map.put("/login-code/login", new Rule(10, 60));
            map.put("/forgot-password/code", new Rule(5, 60));
            map.put("/forgot-password/reset", new Rule(10, 60));
            map.put("/mfa/challenge/send-code", new Rule(5, 60));
            map.put("/mfa/challenge/verify", new Rule(10, 60));
            map.put("/register", new Rule(5, 60));
            return map;
        }

        /** 可信代理配置：默认关闭，仅取 remoteAddr，避免 X-Forwarded-For 被伪造绕过限流。 */
        public static class TrustedProxy {

            private boolean enabled = false;
            /** 信任的反代跳数：从 X-Forwarded-For 右端起第 hops 个为真实客户端。 */
            private int trustedHops = 1;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getTrustedHops() {
                return trustedHops;
            }

            public void setTrustedHops(int trustedHops) {
                this.trustedHops = trustedHops;
            }
        }

        public static class Rule {

            private int limit = 30;
            private int windowSeconds = 60;

            public Rule() {
            }

            public Rule(int limit, int windowSeconds) {
                this.limit = limit;
                this.windowSeconds = windowSeconds;
            }

            public int getLimit() {
                return limit;
            }

            public void setLimit(int limit) {
                this.limit = limit;
            }

            public int getWindowSeconds() {
                return windowSeconds;
            }

            public void setWindowSeconds(int windowSeconds) {
                this.windowSeconds = windowSeconds;
            }
        }
    }
}
