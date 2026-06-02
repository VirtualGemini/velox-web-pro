package com.velox.module.system.mail.service;

import com.velox.module.system.mail.vo.MailChannelActiveReqVO;
import com.velox.module.system.mail.vo.MailChannelRespVO;
import jakarta.validation.Valid;

import java.util.List;

public interface MailChannelService {

    List<MailChannelRespVO> getMailChannelList();

    void updateChannelActive(@Valid MailChannelActiveReqVO reqVO);
}
