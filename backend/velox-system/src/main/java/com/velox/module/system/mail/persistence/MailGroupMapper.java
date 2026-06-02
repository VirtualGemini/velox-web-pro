package com.velox.module.system.mail.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.mail.domain.model.MailGroup;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailGroupMapper extends BaseMapperExt<MailGroup> {
}
