package com.velox.module.system.accesscontrol.service;

import com.velox.module.system.accesscontrol.vo.AccessControlRespVO;

import java.util.List;

/**
 * 访问控制全局配置服务（单例配置）。
 * <p>
 * 管理端用于读写各项全局开关；登录/注册流程用对外查询方法做「后端拒绝」校验。
 */
public interface AccessControlService {

    /** 读取全局配置（懒初始化单例）。 */
    AccessControlRespVO getConfig();

    /** 更新通用注册总开关。 */
    void updateGeneralRegister(boolean enabled);

    /** 更新忘记密码总开关。 */
    void updateForgotPassword(boolean enabled);

    /** 更新已启用的普通登录方式（不可清空到空集）。 */
    void updateLoginMethods(List<String> methods);

    /** 更新已启用的第三方登录渠道。 */
    void updateThirdPartyLogin(List<String> channels);

    /** 更新已启用的第三方注册渠道。 */
    void updateThirdPartyRegister(List<String> channels);

    // ---- 对外查询（双重保护的「后端拒绝」依据） ----

    /** 通用注册是否开启。 */
    boolean isGeneralRegisterEnabled();

    /** 忘记密码是否开启。 */
    boolean isForgotPasswordEnabled();

    /** 当前全局启用的普通登录方式列表（取代 yml 全局配置）。 */
    List<String> getEnabledLoginMethods();
}
