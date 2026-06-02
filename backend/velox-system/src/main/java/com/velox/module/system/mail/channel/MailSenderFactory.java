package com.velox.module.system.mail.channel;

import cn.hutool.core.util.StrUtil;
import com.velox.email.api.message.SendRequest;
import com.velox.email.api.message.SendResponse;
import com.velox.email.support.channel.SmtpEmailChannel;
import com.velox.email.support.meta.SmtpMeta;
import com.velox.email.support.type.ProtocolType;
import com.velox.module.system.mail.domain.model.MailAccount;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发件邮箱 {@link JavaMailSender} 工厂
 * <p>
 * 按账号构建并缓存底层 {@link JavaMailSender}，复用 starter 的 {@link SmtpEmailChannel} 完成实际 MIME 组装与发送。
 * 留空的 host/port/ssl/starttls 通过 {@link SmtpEmailChannel#guessMeta(String)} 按域名识别。
 * 账号配置变化（签名变化）或被编辑/删除时自动失效缓存。
 */
@Component
public class MailSenderFactory {

    private static final String SMTP_PROTOCOL = "smtp";
    private static final String UTF_8 = "UTF-8";
    private static final int DEFAULT_TIMEOUT_MS = 10000;

    private final Map<String, CachedChannel> channelCache = new ConcurrentHashMap<>();

    /**
     * 使用指定账号发送邮件。发件人（from/fromName）以账号配置为准覆盖请求。
     */
    public SendResponse send(MailAccount account, SendRequest request) {
        SmtpEmailChannel channel = resolveChannel(account);
        SendRequest effective = request.toBuilder()
                .from(resolveFrom(account))
                .fromName(account.getFromName())
                .build();
        return channel.send(effective);
    }

    /** 账号被编辑或删除时调用，使缓存的连接失效 */
    public void invalidate(String accountId) {
        if (accountId != null) {
            channelCache.remove(accountId);
        }
    }

    private SmtpEmailChannel resolveChannel(MailAccount account) {
        String signature = signature(account);
        CachedChannel cached = channelCache.get(account.getId());
        if (cached != null && cached.signature.equals(signature)) {
            return cached.channel;
        }
        SmtpEmailChannel channel = new SmtpEmailChannel(buildSender(account));
        channelCache.put(account.getId(), new CachedChannel(signature, channel));
        return channel;
    }

    private JavaMailSender buildSender(MailAccount account) {
        SmtpMeta meta = resolveMeta(account);
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(meta.host());
        sender.setPort(meta.port());
        sender.setUsername(account.getUsername());
        sender.setPassword(account.getPassword());
        sender.setProtocol(SMTP_PROTOCOL);
        sender.setDefaultEncoding(UTF_8);
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", String.valueOf(meta.ssl()));
        props.put("mail.smtp.starttls.enable", String.valueOf(meta.starttls()));
        props.put("mail.smtp.connectiontimeout", String.valueOf(DEFAULT_TIMEOUT_MS));
        props.put("mail.smtp.timeout", String.valueOf(DEFAULT_TIMEOUT_MS));
        props.put("mail.smtp.writetimeout", String.valueOf(DEFAULT_TIMEOUT_MS));
        return sender;
    }

    /**
     * 解析连接元数据：显式配置 host+port 时以显式为准（ssl/starttls 缺省由域名识别补齐），
     * 否则整体按发件域名自动识别。
     */
    private SmtpMeta resolveMeta(MailAccount account) {
        SmtpMeta guessed = SmtpEmailChannel.guessMeta(resolveFrom(account));
        boolean explicitHost = StrUtil.isNotBlank(account.getHost()) && account.getPort() != null;
        String host = explicitHost ? account.getHost() : guessed.host();
        int port = explicitHost ? account.getPort() : guessed.port();
        boolean ssl = account.getSslEnabled() != null ? account.getSslEnabled() == 1 : guessed.ssl();
        boolean starttls = account.getStarttls() != null ? account.getStarttls() == 1 : guessed.starttls();
        ProtocolType protocol = ssl ? ProtocolType.SMTPS : ProtocolType.SMTP;
        return new SmtpMeta(host, port, protocol, ssl, starttls);
    }

    private String resolveFrom(MailAccount account) {
        return StrUtil.isNotBlank(account.getFromAddress()) ? account.getFromAddress() : account.getUsername();
    }

    private String signature(MailAccount account) {
        return String.join("|",
                StrUtil.nullToEmpty(account.getHost()),
                String.valueOf(account.getPort()),
                String.valueOf(account.getSslEnabled()),
                String.valueOf(account.getStarttls()),
                StrUtil.nullToEmpty(account.getUsername()),
                StrUtil.nullToEmpty(account.getPassword()),
                StrUtil.nullToEmpty(account.getFromAddress()));
    }

    private record CachedChannel(String signature, SmtpEmailChannel channel) {
    }
}
