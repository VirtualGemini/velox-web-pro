package com.velox.module.system.verification.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.verification.domain.model.VerificationPolicy;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VerificationPolicyMapper extends BaseMapperExt<VerificationPolicy> {
}
