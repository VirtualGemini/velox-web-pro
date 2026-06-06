package com.velox.module.system.mail.template;

import com.velox.module.system.account.store.AccountLanguageStore;
import cn.hutool.core.util.StrUtil;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * 收件人语言解析。
 * <p>
 * 解析顺序：① 用户保存过的语言偏好（{@link AccountLanguageStore}，zh/en）；
 * ② 否则按当次请求的 Accept-Language（{@link LocaleContextHolder}）：zh* → zh，其它 → en；
 * ③ 兜底 en（「非中国默认英文」）。
 * <p>
 * 注意：必须在请求线程（同步发件）内调用——发件均为 {@code sendSync()}，
 * {@code LocaleContextHolder} 此时有效。
 */
@Component
public class RecipientLanguageResolver {

    private final AccountLanguageStore accountLanguageStore;

    public RecipientLanguageResolver(AccountLanguageStore accountLanguageStore) {
        this.accountLanguageStore = accountLanguageStore;
    }

    public String resolve(String userId) {
        if (StrUtil.isNotBlank(userId)) {
            Optional<String> preference = accountLanguageStore.find(userId);
            if (preference.isPresent()) {
                String normalized = normalize(preference.get());
                if (normalized != null) {
                    return normalized;
                }
            }
        }
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && locale.getLanguage() != null
                && locale.getLanguage().toLowerCase().startsWith("zh")) {
            return "zh";
        }
        return "en";
    }

    private String normalize(String language) {
        if (language == null) {
            return null;
        }
        String value = language.trim().toLowerCase();
        if (value.startsWith("zh")) {
            return "zh";
        }
        if (value.startsWith("en")) {
            return "en";
        }
        return null;
    }
}
