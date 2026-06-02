package com.velox.module.system.mail.service;

import com.velox.common.result.PageResult;
import com.velox.module.system.mail.vo.MailAccountPageReqVO;
import com.velox.module.system.mail.vo.MailAccountRespVO;
import com.velox.module.system.mail.vo.MailAccountSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

public interface MailAccountService {

    String createMailAccount(@Valid MailAccountSaveReqVO createReqVO);

    void updateMailAccount(@Valid MailAccountSaveReqVO updateReqVO);

    void updateMailAccountEnabled(String id, Integer enabled);

    void deleteMailAccount(String id);

    void deleteMailAccountList(List<String> ids);

    MailAccountRespVO getMailAccount(String id);

    PageResult<MailAccountRespVO> getMailAccountPage(MailAccountPageReqVO pageReqVO);

    void recoverMailAccount(String id);

    void testMailAccount(String id, String toEmail);
}
