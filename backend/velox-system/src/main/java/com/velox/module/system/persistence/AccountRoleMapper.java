package com.velox.module.system.persistence;

import com.velox.framework.persistence.api.mapper.BaseMapperExt;
import com.velox.module.system.domain.model.AccountRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

@Mapper
public interface AccountRoleMapper extends BaseMapperExt<AccountRole> {

    @Select("SELECT id, account_id, role_id, create_time, update_time, create_by, update_by, deleted FROM sys_account_role WHERE account_id = #{accountId}")
    List<AccountRole> selectAllByAccountId(@Param("accountId") String accountId);

    @Update({
            "<script>",
            "UPDATE sys_account_role",
            "SET deleted = 1, update_time = CURRENT_TIMESTAMP, update_by = #{operator}",
            "WHERE deleted = 0 AND id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int logicalDeleteByIds(@Param("ids") Collection<String> ids, @Param("operator") String operator);

    @Update({
            "<script>",
            "UPDATE sys_account_role",
            "SET deleted = 0, update_time = CURRENT_TIMESTAMP, update_by = #{operator}",
            "WHERE deleted = 1 AND id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int restoreByIds(@Param("ids") Collection<String> ids, @Param("operator") String operator);
}
