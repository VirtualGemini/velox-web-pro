package com.velox.module.system.mail.template;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;

/**
 * 邮件模板类型
 * <p>
 * 每个类型对应一个业务发件场景，发件时按「类型 + 收件人语言」查模板进行渲染。
 * 枚举名即为存库的 {@code type} code。所有类型共用变量集：
 * {@code username}、{@code code}、{@code validityMinutes}、{@code appName}。
 */
public enum MailTemplateType {

    /** 密码重置验证码（忘记密码，未登录） */
    AUTH_RESET_PASSWORD_CODE,

    /** 登录验证码（邮箱验证码登录，未登录） */
    AUTH_LOGIN_CODE,

    /** 登录二次验证码（登录时的 MFA 挑战） */
    AUTH_LOGIN_MFA_CODE,

    /** 邮箱解绑验证码（账号安全设置内） */
    ACCOUNT_EMAIL_UNBIND_CODE,

    /** 邮箱换绑身份验证（验证当前邮箱） */
    ACCOUNT_EMAIL_REBIND_PROOF_CODE,

    /** 邮箱换绑验证码（发往新邮箱） */
    ACCOUNT_EMAIL_REBIND_CODE,

    /** 二次验证码（账号安全设置内开启/管理邮箱 MFA） */
    ACCOUNT_MFA_CODE;

    /** 存库的类型 code（即枚举名） */
    public String code() {
        return name();
    }

    /** 解析 code，未命中抛 {@link BusinessErrorCode#MAIL_TEMPLATE_TYPE_INVALID} */
    public static MailTemplateType fromCode(String code) {
        for (MailTemplateType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new ApiException(BusinessErrorCode.MAIL_TEMPLATE_TYPE_INVALID);
    }

    public static boolean isValidCode(String code) {
        if (code == null) {
            return false;
        }
        for (MailTemplateType type : values()) {
            if (type.name().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
