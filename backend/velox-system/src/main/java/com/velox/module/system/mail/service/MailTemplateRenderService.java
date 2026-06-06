package com.velox.module.system.mail.service;

import com.velox.module.system.mail.template.MailTemplateType;
import com.velox.module.system.mail.template.RenderedEmail;

import java.util.Map;

/**
 * 邮件模板渲染服务：按类型挑选数据库中的启用模板，替换 {@code {{变量}}} 后返回主题与 HTML。
 */
public interface MailTemplateRenderService {

    /**
     * @param type      邮件类型
     * @param language  收件人语言，保留该参数以兼容现有发信调用链
     * @param variables 模板变量（如 username/code/validityMinutes/appName）
     * @return 渲染后的主题与 HTML 正文
     */
    RenderedEmail render(MailTemplateType type, String language, Map<String, String> variables);
}
