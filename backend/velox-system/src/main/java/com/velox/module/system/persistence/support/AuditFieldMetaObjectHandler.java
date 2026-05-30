package com.velox.module.system.persistence.support;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 审计时间字段自动填充处理器
 * <p>
 * 为继承 {@link com.velox.domain.model.BaseEntity} 的实体在写入时自动填充审计时间字段：
 * <ul>
 *     <li>{@code createTime}：插入时填充（仅当为空，不覆盖调用方手动设置的值）</li>
 *     <li>{@code updateTime}：插入时填充，更新时强制刷新为当前时间</li>
 * </ul>
 * 注意：{@code BaseEntity} 上的 {@code @TableField(fill = ...)} 注解只是声明字段需要填充，
 * 真正执行填充依赖本处理器被注册为 Spring Bean，缺失时所有记录的审计时间将为 NULL。
 * <p>
 * 时区约定：全库时间统一以 UTC 存储，读取时由 {@code RequestDateTimeFormatter} 按请求时区还原。
 * 因此这里必须使用 {@link LocalDateTime#now(java.time.ZoneId)} 取 UTC，而非依赖 JVM 默认时区的
 * {@code LocalDateTime.now()}——否则在 GMT+8 的机器上会写入本地时间，读取时再被 +8，造成展示快 8 小时。
 */
@Component
public class AuditFieldMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        // 严格填充：仅当字段为空时写入，保留调用方可能已手动设置的值
        this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间始终刷新为当前时间，覆盖旧值；hasSetter 兜底基于 Wrapper 的无实体更新
        if (metaObject.hasSetter(UPDATE_TIME)) {
            metaObject.setValue(UPDATE_TIME, LocalDateTime.now(ZoneOffset.UTC));
        }
    }
}
