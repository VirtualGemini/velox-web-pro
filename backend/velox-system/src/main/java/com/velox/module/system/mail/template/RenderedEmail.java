package com.velox.module.system.mail.template;

/**
 * 渲染后的邮件（变量已替换）。
 *
 * @param subject 邮件主题
 * @param html    邮件正文 HTML
 */
public record RenderedEmail(String subject, String html) {
}
