package com.velox.module.system.mail.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.mail.domain.model.MailGroup;
import com.velox.module.system.mail.persistence.MailAccountMapper;
import com.velox.module.system.mail.persistence.MailGroupMapper;
import com.velox.module.system.mail.service.MailGroupService;
import com.velox.module.system.mail.vo.MailGroupBatchDeleteResultVO;
import com.velox.module.system.mail.vo.MailGroupRespVO;
import com.velox.module.system.mail.vo.MailGroupSaveReqVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class MailGroupServiceImpl implements MailGroupService {

    private final MailGroupMapper mailGroupMapper;
    private final MailAccountMapper mailAccountMapper;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MailGroupServiceImpl(MailGroupMapper mailGroupMapper,
                                MailAccountMapper mailAccountMapper,
                                SystemEntityIdGenerator entityIdGenerator,
                                SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.mailGroupMapper = mailGroupMapper;
        this.mailAccountMapper = mailAccountMapper;
        this.entityIdGenerator = entityIdGenerator;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Override
    public List<MailGroupRespVO> getMailGroupList() {
        List<MailGroup> groups = mailGroupMapper.selectList(new LambdaQueryWrapper<MailGroup>()
                .orderByAsc(MailGroup::getSort)
                .orderByAsc(MailGroup::getId));
        return groups.stream().map(this::toRespVO).collect(Collectors.toList());
    }

    @Override
    public String createMailGroup(MailGroupSaveReqVO createReqVO) {
        checkNameUnique(createReqVO.getName(), null);
        MailGroup group = new MailGroup();
        group.setId(entityIdGenerator.nextId(MailGroup.class));
        group.setName(createReqVO.getName());
        group.setActive(createReqVO.getActive() != null ? createReqVO.getActive() : 1);
        group.setSort(createReqVO.getSort() != null ? createReqVO.getSort() : 1);
        group.setRemark(createReqVO.getRemark());
        mailGroupMapper.insert(group);
        return group.getId();
    }

    @Override
    public void updateMailGroup(MailGroupSaveReqVO updateReqVO) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(updateReqVO.getId());
        validateExists(decodedId);
        checkNameUnique(updateReqVO.getName(), decodedId);
        MailGroup updateObj = new MailGroup();
        updateObj.setId(decodedId);
        updateObj.setName(updateReqVO.getName());
        updateObj.setActive(updateReqVO.getActive());
        updateObj.setSort(updateReqVO.getSort());
        updateObj.setRemark(updateReqVO.getRemark());
        mailGroupMapper.updateById(updateObj);
    }

    @Override
    public void deleteMailGroup(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        validateExists(decodedId);
        if (mailAccountMapper.countByGroupId(decodedId) > 0) {
            throw new ApiException(BusinessErrorCode.MAIL_GROUP_IN_USE);
        }
        mailGroupMapper.deleteById(decodedId);
    }

    @Override
    public MailGroupBatchDeleteResultVO deleteMailGroupList(List<String> ids) {
        List<String> decodedIds = frontendIdCodecSupport.decodeIdentifiers(ids);
        MailGroupBatchDeleteResultVO result = new MailGroupBatchDeleteResultVO();

        List<String> inUseNames = new ArrayList<>();
        for (String decodedId : decodedIds) {
            MailGroup group = validateExists(decodedId);
            if (mailAccountMapper.countByGroupId(decodedId) > 0) {
                inUseNames.add(group.getName());
            }
        }

        if (!inUseNames.isEmpty()) {
            result.setInUseNames(inUseNames);
            return result;
        }

        mailGroupMapper.deleteByIds(decodedIds);
        return result;
    }

    private MailGroup validateExists(String id) {
        MailGroup group = mailGroupMapper.selectById(id);
        if (group == null) {
            throw new ApiException(BusinessErrorCode.MAIL_GROUP_NOT_FOUND);
        }
        return group;
    }

    private void checkNameUnique(String name, String excludeId) {
        LambdaQueryWrapper<MailGroup> wrapper = new LambdaQueryWrapper<MailGroup>()
                .eq(MailGroup::getName, name);
        if (excludeId != null) {
            wrapper.ne(MailGroup::getId, excludeId);
        }
        if (mailGroupMapper.selectCount(wrapper) > 0) {
            throw new ApiException(BusinessErrorCode.MAIL_GROUP_NAME_DUPLICATE);
        }
    }

    private MailGroupRespVO toRespVO(MailGroup group) {
        MailGroupRespVO respVO = new MailGroupRespVO();
        respVO.setId(group.getId());
        respVO.setName(group.getName());
        respVO.setActive(group.getActive());
        respVO.setSort(group.getSort());
        respVO.setRemark(group.getRemark());
        respVO.setAccountCount(mailAccountMapper.countByGroupId(group.getId()));
        respVO.setCreateTime(RequestDateTimeFormatter.format(group.getCreateTime()));
        respVO.setUpdateTime(RequestDateTimeFormatter.format(group.getUpdateTime()));
        return respVO;
    }
}
