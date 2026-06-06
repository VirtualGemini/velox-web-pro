package com.velox.module.system.mail.template;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;

/**
 * 邮件模板类型
 */
public enum MailTemplateKind {

    SYSTEM,

    CUSTOM;

    public String code() {
        return name();
    }

    public static MailTemplateKind fromCode(String code) {
        for (MailTemplateKind kind : values()) {
            if (kind.name().equals(code)) {
                return kind;
            }
        }
        throw new ApiException(BusinessErrorCode.MAIL_TEMPLATE_TYPE_INVALID);
    }
}
