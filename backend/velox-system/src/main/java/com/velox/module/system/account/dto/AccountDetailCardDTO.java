package com.velox.module.system.account.dto;

import java.util.List;

public class AccountDetailCardDTO {

    private Header header;

    private ProfileSection profile;

    private AccountSection account;

    private SecuritySection security;

    private List<ThirdPartyProvider> thirdPartyProviders;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public ProfileSection getProfile() {
        return profile;
    }

    public void setProfile(ProfileSection profile) {
        this.profile = profile;
    }

    public AccountSection getAccount() {
        return account;
    }

    public void setAccount(AccountSection account) {
        this.account = account;
    }

    public SecuritySection getSecurity() {
        return security;
    }

    public void setSecurity(SecuritySection security) {
        this.security = security;
    }

    public List<ThirdPartyProvider> getThirdPartyProviders() {
        return thirdPartyProviders;
    }

    public void setThirdPartyProviders(List<ThirdPartyProvider> thirdPartyProviders) {
        this.thirdPartyProviders = thirdPartyProviders;
    }

    public static class Header {
        private String avatar;
        private String username;
        private String nickname;
        private String realName;
        private String status;
        private String activeStatus;
        private String remark;
        private List<String> roleCodes;
        private String createTime;
        private String updateTime;

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getActiveStatus() {
            return activeStatus;
        }

        public void setActiveStatus(String activeStatus) {
            this.activeStatus = activeStatus;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public List<String> getRoleCodes() {
            return roleCodes;
        }

        public void setRoleCodes(List<String> roleCodes) {
            this.roleCodes = roleCodes;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }
    }

    public static class ProfileSection {
        private String nickname;
        private Integer gender;
        private String realName;
        private String displayEmail;
        private String displayMobile;
        private String address;
        private String position;
        private String company;
        private String signature;
        private String introduction;
        private List<String> tags;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public Integer getGender() {
            return gender;
        }

        public void setGender(Integer gender) {
            this.gender = gender;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getDisplayEmail() {
            return displayEmail;
        }

        public void setDisplayEmail(String displayEmail) {
            this.displayEmail = displayEmail;
        }

        public String getDisplayMobile() {
            return displayMobile;
        }

        public void setDisplayMobile(String displayMobile) {
            this.displayMobile = displayMobile;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    public static class AccountSection {
        private String accountId;
        private String username;
        private String remark;
        private String status;
        private String activeStatus;
        private Boolean pendingDeletion;
        private String deletionRequestedAt;
        private String deletionExpiresAt;
        private Integer loginFailCount;
        private String loginFailTime;
        private List<String> roleCodes;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getActiveStatus() {
            return activeStatus;
        }

        public void setActiveStatus(String activeStatus) {
            this.activeStatus = activeStatus;
        }

        public Boolean getPendingDeletion() {
            return pendingDeletion;
        }

        public void setPendingDeletion(Boolean pendingDeletion) {
            this.pendingDeletion = pendingDeletion;
        }

        public String getDeletionRequestedAt() {
            return deletionRequestedAt;
        }

        public void setDeletionRequestedAt(String deletionRequestedAt) {
            this.deletionRequestedAt = deletionRequestedAt;
        }

        public String getDeletionExpiresAt() {
            return deletionExpiresAt;
        }

        public void setDeletionExpiresAt(String deletionExpiresAt) {
            this.deletionExpiresAt = deletionExpiresAt;
        }

        public Integer getLoginFailCount() {
            return loginFailCount;
        }

        public void setLoginFailCount(Integer loginFailCount) {
            this.loginFailCount = loginFailCount;
        }

        public String getLoginFailTime() {
            return loginFailTime;
        }

        public void setLoginFailTime(String loginFailTime) {
            this.loginFailTime = loginFailTime;
        }

        public List<String> getRoleCodes() {
            return roleCodes;
        }

        public void setRoleCodes(List<String> roleCodes) {
            this.roleCodes = roleCodes;
        }
    }

    public static class SecuritySection {
        private String securityEmail;
        private Boolean emailMfaEnabled;
        private Boolean totpMfaEnabled;
        private List<String> loginMethods;
        private List<String> disabledLoginMethods;
        private List<String> allowedLoginMethods;
        private String emailVerifiedAt;
        private String lastPasswordChangeAt;

        public String getSecurityEmail() {
            return securityEmail;
        }

        public void setSecurityEmail(String securityEmail) {
            this.securityEmail = securityEmail;
        }

        public Boolean getEmailMfaEnabled() {
            return emailMfaEnabled;
        }

        public void setEmailMfaEnabled(Boolean emailMfaEnabled) {
            this.emailMfaEnabled = emailMfaEnabled;
        }

        public Boolean getTotpMfaEnabled() {
            return totpMfaEnabled;
        }

        public void setTotpMfaEnabled(Boolean totpMfaEnabled) {
            this.totpMfaEnabled = totpMfaEnabled;
        }

        public List<String> getLoginMethods() {
            return loginMethods;
        }

        public void setLoginMethods(List<String> loginMethods) {
            this.loginMethods = loginMethods;
        }

        public List<String> getDisabledLoginMethods() {
            return disabledLoginMethods;
        }

        public void setDisabledLoginMethods(List<String> disabledLoginMethods) {
            this.disabledLoginMethods = disabledLoginMethods;
        }

        public List<String> getAllowedLoginMethods() {
            return allowedLoginMethods;
        }

        public void setAllowedLoginMethods(List<String> allowedLoginMethods) {
            this.allowedLoginMethods = allowedLoginMethods;
        }

        public String getEmailVerifiedAt() {
            return emailVerifiedAt;
        }

        public void setEmailVerifiedAt(String emailVerifiedAt) {
            this.emailVerifiedAt = emailVerifiedAt;
        }

        public String getLastPasswordChangeAt() {
            return lastPasswordChangeAt;
        }

        public void setLastPasswordChangeAt(String lastPasswordChangeAt) {
            this.lastPasswordChangeAt = lastPasswordChangeAt;
        }
    }

    public static class ThirdPartyProvider {
        private String key;
        private String name;
        private Boolean bound;
        private Boolean disabled;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getBound() {
            return bound;
        }

        public void setBound(Boolean bound) {
            this.bound = bound;
        }

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
        }
    }
}
