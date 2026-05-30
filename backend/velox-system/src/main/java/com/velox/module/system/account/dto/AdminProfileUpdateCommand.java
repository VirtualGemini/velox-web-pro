package com.velox.module.system.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 管理员编辑他人资料的命令。字段全部可选（均非必填），手机号校验较自助端宽松，
 * 避免历史脏数据阻断管理员保存。包含头像（自助端头像走独立接口，管理员随资料一起保存）。
 */
public class AdminProfileUpdateCommand {

    @Size(max = 50, message = "{validation.system.user.profile.real_name.size}")
    private String realName;

    @Size(max = 50, message = "{validation.system.user.profile.nickname.size}")
    private String nickname;

    @Email(message = "{validation.system.user.profile.email.invalid}")
    @Size(max = 100, message = "{validation.system.user.profile.email.size}")
    private String email;

    @Size(max = 20, message = "{validation.system.user.profile.phone.pattern}")
    private String phone;

    @Size(max = 255, message = "{validation.system.user.profile.address.size}")
    private String address;

    private Integer gender;

    @Size(max = 1000, message = "{validation.system.user.profile.introduction.size}")
    private String introduction;

    @Size(max = 255, message = "{validation.system.user.profile.signature.size}")
    private String signature;

    @Size(max = 50, message = "{validation.system.user.profile.position.size}")
    private String position;

    @Size(max = 100, message = "{validation.system.user.profile.company.size}")
    private String company;

    /** 头像 URL（由通用上传接口生成，随资料一起保存）。 */
    private String avatar;

    private List<@Size(max = 16, message = "{validation.system.user.profile.tags.item.size}") String> tags;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
