package com.velox.module.system.mail.channel;

import com.velox.email.api.message.SendRequest;
import com.velox.email.api.message.SendResponse;
import com.velox.email.spi.channel.EmailChannel;
import com.velox.module.system.mail.domain.model.MailAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 发件池通道
 * <p>
 * 实现 starter 的 {@link EmailChannel} 扩展点并以 {@code @Component} 注册。由于本 Bean 早于 starter
 * 自动配置装配，starter 的 {@code veloxEmailJavaMailSender}/{@code SmtpEmailChannel}（均带
 * {@code @ConditionalOnMissingBean(EmailChannel.class)} 链路）会自动退让，而 starter 仍以
 * {@code DefaultEmailSender} 包装本通道，从而保留 retry/async/interceptor/listener 能力，业务调用点零改动。
 * <p>
 * {@code @ConditionalOnProperty(velox.email.enabled=true)} 保证关闭邮件能力时与 starter 的 NoOp 实现不冲突。
 * <p>
 * 发送时按权重从池中挑选可用邮箱，失败则自动切换到下一个可用邮箱（在本次发送内对每个可用邮箱至多尝试一次），
 * 并将每次结果回写到健康状态机。
 */
@Component
@ConditionalOnProperty(prefix = "velox.email", name = "enabled", havingValue = "true")
public class PooledMailChannel implements EmailChannel {

    private static final Logger log = LoggerFactory.getLogger(PooledMailChannel.class);

    private static final String NAME = "pool";

    private final MailAccountSelector selector;
    private final MailSenderFactory senderFactory;

    public PooledMailChannel(MailAccountSelector selector, MailSenderFactory senderFactory) {
        this.selector = selector;
        this.senderFactory = senderFactory;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public SendResponse send(SendRequest request) {
        Set<String> tried = new HashSet<>();
        SendResponse lastFailure = null;
        MailAccount account;
        while ((account = selector.selectNext(tried)) != null) {
            tried.add(account.getId());
            SendResponse response = trySend(account, request);
            if (response.success()) {
                selector.onSuccess(account.getId());
                return response;
            }
            selector.onFailure(account.getId());
            lastFailure = response;
            log.warn("Mail account [{}] failed to send, trying next available account: {}",
                    account.getName(), response.error());
        }
        if (lastFailure != null) {
            return lastFailure;
        }
        log.warn("No available mail account in the sending pool");
        return SendResponse.builder()
                .success(false)
                .error("No available mail account in the sending pool")
                .build();
    }

    private SendResponse trySend(MailAccount account, SendRequest request) {
        try {
            return senderFactory.send(account, request);
        } catch (Exception exception) {
            return SendResponse.builder()
                    .success(false)
                    .error(exception.getMessage() == null
                            ? exception.getClass().getSimpleName() : exception.getMessage())
                    .build();
        }
    }
}
