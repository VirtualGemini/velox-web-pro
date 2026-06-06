package com.velox.module.system.mail.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.module.system.mail.domain.model.MailTemplate;
import com.velox.module.system.mail.persistence.MailTemplateMapper;
import com.velox.module.system.mail.service.MailTemplateRenderService;
import com.velox.module.system.mail.template.MailTemplateType;
import com.velox.module.system.mail.template.RenderedEmail;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MailTemplateRenderServiceImpl implements MailTemplateRenderService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\}");

    private final MailTemplateMapper mailTemplateMapper;

    public MailTemplateRenderServiceImpl(MailTemplateMapper mailTemplateMapper) {
        this.mailTemplateMapper = mailTemplateMapper;
    }

    @Override
    public RenderedEmail render(MailTemplateType type, String language, Map<String, String> variables) {
        MailTemplate row = resolveTemplate(type);
        Map<String, String> vars = variables != null ? variables : Map.of();
        String subject = substitute(row.getSubject(), vars, false);
        String html = substitute(row.getContent(), vars, true);
        return new RenderedEmail(subject, html);
    }

    /** 只允许从数据库启用模板渲染，不再回退到代码内置模板。 */
    private MailTemplate resolveTemplate(MailTemplateType type) {
        MailTemplate row = mailTemplateMapper.selectEnabledBySendType(type.code());
        if (row == null) {
            throw new ApiException(BusinessErrorCode.MAIL_TEMPLATE_NOT_FOUND);
        }
        return row;
    }

    /** 替换 {@code {{key}}} 占位；HTML 正文对变量值做转义，主题保持原值 */
    private String substitute(String template, Map<String, String> variables, boolean escapeHtml) {
        if (template == null) {
            return "";
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.get(key);
            if (value == null) {
                value = "";
            }
            if (escapeHtml) {
                value = HtmlUtils.htmlEscape(value);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
