package com.velox.module.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录 captcha 一次性票据")
public class CaptchaTicketDTO {

    @Schema(description = "一次性 captcha 票据")
    private String captchaTicket;

    @Schema(description = "票据有效期（秒）")
    private int ttl;

    public CaptchaTicketDTO() {
    }

    public CaptchaTicketDTO(String captchaTicket, int ttl) {
        this.captchaTicket = captchaTicket;
        this.ttl = ttl;
    }

    public String getCaptchaTicket() {
        return captchaTicket;
    }

    public void setCaptchaTicket(String captchaTicket) {
        this.captchaTicket = captchaTicket;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
}
