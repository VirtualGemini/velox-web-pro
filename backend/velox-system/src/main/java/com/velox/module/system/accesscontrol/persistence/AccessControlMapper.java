package com.velox.module.system.accesscontrol.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.accesscontrol.domain.model.AccessControlConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessControlMapper extends BaseMapperExt<AccessControlConfig> {
}
