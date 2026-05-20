package com.velox.module.system.auth.store;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.velox.framework.security.properties.SecurityProperties;

import java.nio.charset.StandardCharsets;

abstract class AbstractVerificationCodeStore implements VerificationCodeStore {

    protected static final String CAPTCHA_PREFIX = "auth:captcha:";
    protected static final String RESET_PREFIX = "auth:reset:";
    protected static final String RESET_SENT_PREFIX = "auth:reset:sent:";

    protected final SecurityProperties securityProperties;

    protected AbstractVerificationCodeStore(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    protected String digest(String code) {
        String secret = securityProperties.getVerification().getSecret();
        HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
        return hMac.digestHex(code == null ? "" : code.trim().toLowerCase());
    }
}
