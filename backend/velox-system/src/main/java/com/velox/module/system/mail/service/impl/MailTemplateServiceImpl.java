package com.velox.module.system.mail.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.mail.domain.model.MailTemplate;
import com.velox.module.system.mail.persistence.MailTemplateMapper;
import com.velox.module.system.mail.service.MailTemplateService;
import com.velox.module.system.mail.template.MailTemplateKind;
import com.velox.module.system.mail.template.MailTemplateType;
import com.velox.module.system.mail.vo.MailTemplatePageReqVO;
import com.velox.module.system.mail.vo.MailTemplateRespVO;
import com.velox.module.system.mail.vo.MailTemplateSaveReqVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class MailTemplateServiceImpl implements MailTemplateService {

    private static final String TEMPLATE_TYPE_SYSTEM = MailTemplateKind.SYSTEM.code();
    private static final String TEMPLATE_TYPE_CUSTOM = MailTemplateKind.CUSTOM.code();

    private final MailTemplateMapper mailTemplateMapper;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MailTemplateServiceImpl(MailTemplateMapper mailTemplateMapper,
                                   SystemEntityIdGenerator entityIdGenerator,
                                   SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.mailTemplateMapper = mailTemplateMapper;
        this.entityIdGenerator = entityIdGenerator;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createMailTemplate(MailTemplateSaveReqVO createReqVO) {
        validateSendType(createReqVO.getSendType());
        validateNameUnique(createReqVO.getName(), null);

        TemplateContent content = normalizeContent(createReqVO, null);
        String templateType = StrUtil.blankToDefault(createReqVO.getTemplateType(), TEMPLATE_TYPE_CUSTOM);
        validateTemplateType(templateType);

        Integer enabled = createReqVO.getEnabled() != null ? createReqVO.getEnabled() : 1;
        if (enabled == 1) {
            // 同一发件类型仅允许一条启用模板。
            disableSiblings(createReqVO.getSendType(), null);
        }

        MailTemplate template = new MailTemplate();
        template.setId(entityIdGenerator.nextId(MailTemplate.class));
        template.setName(createReqVO.getName());
        template.setSendType(createReqVO.getSendType());
        template.setTemplateType(templateType);
        template.setSubject(content.subject());
        template.setContent(content.content());
        template.setEnabled(enabled);
        template.setSort(createReqVO.getSort() != null ? createReqVO.getSort() : 1);
        template.setRemark(createReqVO.getRemark());
        mailTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMailTemplate(MailTemplateSaveReqVO updateReqVO) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(updateReqVO.getId());
        MailTemplate existing = validateExists(decodedId);
        validateNameUnique(updateReqVO.getName(), decodedId);

        // 发件类型、模板类型不可变：系统内置模板保持内置，自定义模板保持自定义。
        String sendType = existing.getSendType();
        TemplateContent content = normalizeContent(updateReqVO, existing);
        Integer enabled = updateReqVO.getEnabled() != null ? updateReqVO.getEnabled() : existing.getEnabled();
        if (enabled != null && enabled == 1) {
            disableSiblings(sendType, decodedId);
        }

        mailTemplateMapper.update(null, new LambdaUpdateWrapper<MailTemplate>()
                .eq(MailTemplate::getId, decodedId)
                .set(MailTemplate::getName, updateReqVO.getName())
                .set(MailTemplate::getSubject, content.subject())
                .set(MailTemplate::getContent, content.content())
                .set(MailTemplate::getEnabled, enabled)
                .set(MailTemplate::getSort, updateReqVO.getSort() != null ? updateReqVO.getSort() : existing.getSort())
                .set(MailTemplate::getRemark, updateReqVO.getRemark())
                .set(MailTemplate::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMailTemplateEnabled(String id, Integer enabled) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        MailTemplate existing = validateExists(decodedId);
        if (enabled != null && enabled == 1) {
            disableSiblings(existing.getSendType(), decodedId);
        }
        mailTemplateMapper.update(null, new LambdaUpdateWrapper<MailTemplate>()
                .eq(MailTemplate::getId, decodedId)
                .set(MailTemplate::getEnabled, enabled)
                .set(MailTemplate::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
    }

    @Override
    public void deleteMailTemplate(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        MailTemplate existing = validateExists(decodedId);
        validateDeleteAllowed(existing);
        mailTemplateMapper.deleteById(decodedId);
    }

    @Override
    public void deleteMailTemplateList(List<String> ids) {
        List<String> decodedIds = frontendIdCodecSupport.decodeIdentifiers(ids);
        if (decodedIds.isEmpty()) {
            return;
        }
        List<MailTemplate> targets = mailTemplateMapper.selectListByIds(decodedIds);
        targets.forEach(this::validateDeleteAllowed);
        mailTemplateMapper.deleteByIds(decodedIds);
    }

    @Override
    public MailTemplateRespVO getMailTemplate(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        MailTemplate template = mailTemplateMapper.selectById(decodedId);
        if (template == null) {
            return null;
        }
        return toRespVO(template, true);
    }

    @Override
    public PageResult<MailTemplateRespVO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO) {
        LambdaQueryWrapper<MailTemplate> wrapper = new LambdaQueryWrapper<MailTemplate>()
                .like(StrUtil.isNotEmpty(pageReqVO.getName()), MailTemplate::getName, pageReqVO.getName())
                .eq(StrUtil.isNotEmpty(pageReqVO.getSendType()), MailTemplate::getSendType, pageReqVO.getSendType())
                .eq(StrUtil.isNotEmpty(pageReqVO.getTemplateType()), MailTemplate::getTemplateType, pageReqVO.getTemplateType())
                .eq(pageReqVO.getEnabled() != null, MailTemplate::getEnabled, pageReqVO.getEnabled())
                .like(StrUtil.isNotEmpty(pageReqVO.getRemark()), MailTemplate::getRemark, pageReqVO.getRemark())
                .ge(StrUtil.isNotBlank(pageReqVO.getCreateTimeStart()), MailTemplate::getCreateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getCreateTimeStart()))
                .le(StrUtil.isNotBlank(pageReqVO.getCreateTimeEnd()), MailTemplate::getCreateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getCreateTimeEnd()))
                .ge(StrUtil.isNotBlank(pageReqVO.getUpdateTimeStart()), MailTemplate::getUpdateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getUpdateTimeStart()))
                .le(StrUtil.isNotBlank(pageReqVO.getUpdateTimeEnd()), MailTemplate::getUpdateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getUpdateTimeEnd()))
                .orderByDesc(MailTemplate::getCreateTime)
                .orderByDesc(MailTemplate::getId);
        Page<MailTemplate> page = mailTemplateMapper.selectPage(
                new Page<>(pageReqVO.getPage(), pageReqVO.getSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream()
                        .map(template -> toRespVO(template, false))
                        .collect(Collectors.toList()));
    }

    /** 将同一发件类型下其它已启用模板置为停用。 */
    private void disableSiblings(String sendType, String excludeId) {
        mailTemplateMapper.update(null, new LambdaUpdateWrapper<MailTemplate>()
                .eq(MailTemplate::getSendType, sendType)
                .eq(MailTemplate::getEnabled, 1)
                .ne(StrUtil.isNotEmpty(excludeId), MailTemplate::getId, excludeId)
                .set(MailTemplate::getEnabled, 0)
                .set(MailTemplate::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
    }

    private MailTemplate validateExists(String id) {
        MailTemplate template = mailTemplateMapper.selectById(id);
        if (template == null) {
            throw new ApiException(BusinessErrorCode.MAIL_TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    private void validateSendType(String sendType) {
        MailTemplateType.fromCode(sendType);
    }

    private void validateTemplateType(String templateType) {
        MailTemplateKind.fromCode(templateType);
    }

    private void validateNameUnique(String name, String excludeId) {
        MailTemplate found = mailTemplateMapper.selectByName(name);
        if (found != null && !found.getId().equals(excludeId)) {
            throw new ApiException(BusinessErrorCode.MAIL_TEMPLATE_NAME_DUPLICATE);
        }
    }

    private void validateDeleteAllowed(MailTemplate template) {
        if (TEMPLATE_TYPE_SYSTEM.equals(template.getTemplateType())) {
            throw new ApiException(BusinessErrorCode.MAIL_TEMPLATE_SYSTEM_BUILTIN_DELETE_FORBIDDEN);
        }
    }

    private TemplateContent normalizeContent(MailTemplateSaveReqVO req, MailTemplate existing) {
        String subject = trimToNull(req.getSubject(), existing == null ? null : existing.getSubject());
        String content = trimToNull(req.getContent(), existing == null ? null : existing.getContent());
        if (StrUtil.isBlank(subject) || StrUtil.isBlank(content)) {
            throw new ApiException(BusinessErrorCode.MAIL_TEMPLATE_CONTENT_EMPTY);
        }
        return new TemplateContent(subject, content);
    }

    private String trimToNull(String value, String fallback) {
        return value == null ? fallback : StrUtil.blankToDefault(value.trim(), null);
    }

    private MailTemplateRespVO toRespVO(MailTemplate template, boolean includeContent) {
        MailTemplateRespVO respVO = new MailTemplateRespVO();
        respVO.setId(template.getId());
        respVO.setName(template.getName());
        respVO.setSendType(template.getSendType());
        respVO.setType(template.getSendType());
        respVO.setTemplateType(template.getTemplateType());
        respVO.setSubject(template.getSubject());
        if (includeContent) {
            respVO.setContent(template.getContent());
        }
        respVO.setEnabled(template.getEnabled());
        respVO.setSort(template.getSort());
        respVO.setRemark(template.getRemark());
        respVO.setCreateTime(RequestDateTimeFormatter.format(template.getCreateTime()));
        respVO.setUpdateTime(RequestDateTimeFormatter.format(template.getUpdateTime()));
        return respVO;
    }

    private record TemplateContent(String subject, String content) {
    }
}
