package com.velox.module.system.mail.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.mail.domain.model.MailTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface MailTemplateMapper extends BaseMapperExt<MailTemplate> {

    /** 取某发件类型下启用的模板（发件用）；若多条取最近更新的一条 */
    default MailTemplate selectEnabledBySendType(String sendType) {
        return selectOne(new LambdaQueryWrapper<MailTemplate>()
                .eq(MailTemplate::getSendType, sendType)
                .eq(MailTemplate::getEnabled, 1)
                .orderByDesc(MailTemplate::getUpdateTime)
                .orderByDesc(MailTemplate::getId)
                .last("limit 1"));
    }

    /** 统计某发件类型的模板数量（删除保底用） */
    default Long countBySendType(String sendType) {
        return selectCount(new LambdaQueryWrapper<MailTemplate>()
                .eq(MailTemplate::getSendType, sendType));
    }

    /** 按名称查模板（名称唯一校验用） */
    default MailTemplate selectByName(String name) {
        return selectOne(new LambdaQueryWrapper<MailTemplate>()
                .eq(MailTemplate::getName, name)
                .last("limit 1"));
    }

    /** 按 id 集合批量取（批量删除保底统计用，避开 selectByIds/selectBatchIds 版本差异） */
    default List<MailTemplate> selectListByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<MailTemplate>()
                .in(MailTemplate::getId, ids));
    }
}
