package com.velox.module.system.mail.service;

import com.velox.module.system.mail.vo.MailGroupBatchDeleteResultVO;
import com.velox.module.system.mail.vo.MailGroupRespVO;
import com.velox.module.system.mail.vo.MailGroupSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

public interface MailGroupService {

    List<MailGroupRespVO> getMailGroupList();

    String createMailGroup(@Valid MailGroupSaveReqVO createReqVO);

    void updateMailGroup(@Valid MailGroupSaveReqVO updateReqVO);

    void deleteMailGroup(String id);

    MailGroupBatchDeleteResultVO deleteMailGroupList(List<String> ids);
}
