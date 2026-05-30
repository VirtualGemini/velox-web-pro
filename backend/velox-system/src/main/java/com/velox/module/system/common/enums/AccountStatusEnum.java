package com.velox.module.system.common.enums;

/**
 * 账号状态（账号生命周期，区别于在线/离线的活跃状态）。
 * 启用可正常登录；禁用、注销禁止登录；异常仅作标记，不阻断登录。
 */
public enum AccountStatusEnum {

    ENABLED(1, "启用"),
    DISABLED(2, "禁用"),
    ABNORMAL(3, "异常"),
    CANCELLED(4, "注销");

    private final int code;
    private final String desc;

    AccountStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDesc(Integer code) {
        if (code == null) {
            return "";
        }
        for (AccountStatusEnum item : values()) {
            if (item.code == code) {
                return item.desc;
            }
        }
        return "";
    }

    /** 是否为合法的账号状态码。 */
    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        for (AccountStatusEnum item : values()) {
            if (item.code == code) {
                return true;
            }
        }
        return false;
    }

    /** 该状态是否禁止登录（禁用、注销）。 */
    public static boolean isLoginBlocked(Integer code) {
        return code != null && (DISABLED.code == code || CANCELLED.code == code);
    }

    /** 转换为前端使用的字符串状态码，未知值回落为启用。 */
    public static String toStringCode(Integer code) {
        return String.valueOf(isValid(code) ? code : ENABLED.code);
    }
}
