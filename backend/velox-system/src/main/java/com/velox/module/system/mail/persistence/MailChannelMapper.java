package com.velox.module.system.mail.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.mail.domain.model.MailChannel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MailChannelMapper extends BaseMapperExt<MailChannel> {

    default List<MailChannel> selectListOrderBySort() {
        return selectList(new LambdaQueryWrapper<MailChannel>()
                .orderByAsc(MailChannel::getSort)
                .orderByAsc(MailChannel::getId));
    }
}
