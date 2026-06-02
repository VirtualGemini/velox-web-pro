package com.velox.module.system.mail.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.mail.domain.model.MailAccount;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MailAccountMapper extends BaseMapperExt<MailAccount> {

    default Long countByGroupId(String groupId) {
        return selectCount(new LambdaQueryWrapper<MailAccount>()
                .eq(MailAccount::getGroupId, groupId));
    }

    default Long countByChannelId(String channelId) {
        return selectCount(new LambdaQueryWrapper<MailAccount>()
                .eq(MailAccount::getChannelId, channelId));
    }

    default List<MailAccount> selectListByEnabled(Integer enabled) {
        return selectList(new LambdaQueryWrapper<MailAccount>()
                .eq(MailAccount::getEnabled, enabled));
    }
}
