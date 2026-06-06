package com.velox.module.system.mail.template;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;

/**
 * 邮件模板语言
 */
public enum MailTemplateLanguage {

    ZH("zh"),

    EN("en");

    private final String code;

    MailTemplateLanguage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static MailTemplateLanguage fromCode(String code) {
        for (MailTemplateLanguage language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        throw new ApiException(BusinessErrorCode.MAIL_TEMPLATE_LANGUAGE_INVALID);
    }
}
