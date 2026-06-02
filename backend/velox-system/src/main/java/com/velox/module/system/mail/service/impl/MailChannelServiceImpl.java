package com.velox.module.system.mail.service.impl;

import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.mail.domain.model.MailChannel;
import com.velox.module.system.mail.persistence.MailAccountMapper;
import com.velox.module.system.mail.persistence.MailChannelMapper;
import com.velox.module.system.mail.service.MailChannelService;
import com.velox.module.system.mail.vo.MailChannelActiveReqVO;
import com.velox.module.system.mail.vo.MailChannelRespVO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class MailChannelServiceImpl implements MailChannelService {

    private final MailChannelMapper mailChannelMapper;
    private final MailAccountMapper mailAccountMapper;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MailChannelServiceImpl(MailChannelMapper mailChannelMapper,
                                  MailAccountMapper mailAccountMapper,
                                  SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.mailChannelMapper = mailChannelMapper;
        this.mailAccountMapper = mailAccountMapper;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Override
    public List<MailChannelRespVO> getMailChannelList() {
        return mailChannelMapper.selectListOrderBySort().stream()
                .map(this::toRespVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannelActive(MailChannelActiveReqVO reqVO) {
        List<String> activeIds = reqVO.getActiveChannelIds() == null
                ? Collections.emptyList()
                : frontendIdCodecSupport.decodeIdentifiers(reqVO.getActiveChannelIds());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        // 未选中的渠道置为不生效
        LambdaUpdateWrapper<MailChannel> deactivate = new LambdaUpdateWrapper<MailChannel>()
                .set(MailChannel::getActive, 0)
                .set(MailChannel::getUpdateTime, now);
        if (!activeIds.isEmpty()) {
            deactivate.notIn(MailChannel::getId, activeIds);
        }
        mailChannelMapper.update(deactivate);
        // 选中的渠道置为生效
        if (!activeIds.isEmpty()) {
            mailChannelMapper.update(new LambdaUpdateWrapper<MailChannel>()
                    .set(MailChannel::getActive, 1)
                    .set(MailChannel::getUpdateTime, now)
                    .in(MailChannel::getId, activeIds));
        }
    }

    private MailChannelRespVO toRespVO(MailChannel channel) {
        MailChannelRespVO respVO = new MailChannelRespVO();
        respVO.setId(channel.getId());
        respVO.setName(channel.getName());
        respVO.setProtocol(channel.getProtocol());
        respVO.setActive(channel.getActive());
        respVO.setSort(channel.getSort());
        respVO.setRemark(channel.getRemark());
        respVO.setAccountCount(mailAccountMapper.countByChannelId(channel.getId()));
        return respVO;
    }
}
