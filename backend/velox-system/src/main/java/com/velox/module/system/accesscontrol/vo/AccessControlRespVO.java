package com.velox.module.system.accesscontrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "openapi.settings.access-control.resp.schema")
public class AccessControlRespVO {

    @Schema(description = "openapi.settings.access-control.resp.general_register_enabled")
    private Boolean generalRegisterEnabled;

    @Schema(description = "openapi.settings.access-control.resp.forgot_password_enabled")
    private Boolean forgotPasswordEnabled;

    @Schema(description = "openapi.settings.access-control.resp.login_methods")
    private List<String> loginMethods;

    @Schema(description = "openapi.settings.access-control.resp.third_party_login_channels")
    private List<String> thirdPartyLoginChannels;

    @Schema(description = "openapi.settings.access-control.resp.third_party_register_channels")
    private List<String> thirdPartyRegisterChannels;

    public Boolean getGeneralRegisterEnabled() {
        return generalRegisterEnabled;
    }

    public void setGeneralRegisterEnabled(Boolean generalRegisterEnabled) {
        this.generalRegisterEnabled = generalRegisterEnabled;
    }

    public Boolean getForgotPasswordEnabled() {
        return forgotPasswordEnabled;
    }

    public void setForgotPasswordEnabled(Boolean forgotPasswordEnabled) {
        this.forgotPasswordEnabled = forgotPasswordEnabled;
    }

    public List<String> getLoginMethods() {
        return loginMethods;
    }

    public void setLoginMethods(List<String> loginMethods) {
        this.loginMethods = loginMethods;
    }

    public List<String> getThirdPartyLoginChannels() {
        return thirdPartyLoginChannels;
    }

    public void setThirdPartyLoginChannels(List<String> thirdPartyLoginChannels) {
        this.thirdPartyLoginChannels = thirdPartyLoginChannels;
    }

    public List<String> getThirdPartyRegisterChannels() {
        return thirdPartyRegisterChannels;
    }

    public void setThirdPartyRegisterChannels(List<String> thirdPartyRegisterChannels) {
        this.thirdPartyRegisterChannels = thirdPartyRegisterChannels;
    }
}
