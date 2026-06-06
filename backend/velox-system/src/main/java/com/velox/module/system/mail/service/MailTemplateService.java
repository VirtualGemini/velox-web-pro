package com.velox.module.system.mail.service;

import com.velox.common.result.PageResult;
import com.velox.module.system.mail.vo.MailTemplatePageReqVO;
import com.velox.module.system.mail.vo.MailTemplateRespVO;
import com.velox.module.system.mail.vo.MailTemplateSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

public interface MailTemplateService {

    String createMailTemplate(@Valid MailTemplateSaveReqVO createReqVO);

    void updateMailTemplate(@Valid MailTemplateSaveReqVO updateReqVO);

    void updateMailTemplateEnabled(String id, Integer enabled);

    void deleteMailTemplate(String id);

    void deleteMailTemplateList(List<String> ids);

    MailTemplateRespVO getMailTemplate(String id);

    PageResult<MailTemplateRespVO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO);
}
