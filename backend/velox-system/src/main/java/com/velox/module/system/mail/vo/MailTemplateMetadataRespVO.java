package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "openapi.system.mail_template.metadata_resp.schema")
public class MailTemplateMetadataRespVO {

    @Schema(description = "openapi.system.mail_template.field.send_type")
    private List<String> sendTypes;

    @Schema(description = "openapi.system.mail_template.field.template_type")
    private List<String> templateTypes;

    @Schema(description = "openapi.system.mail_template.field.preview_variables")
    private List<String> variables;

    @Schema(description = "openapi.system.mail_template.field.validity_minutes")
    private Map<String, Integer> validityMinutes;

    @Schema(description = "openapi.system.mail_template.field.preview_sample")
    private MailTemplatePreviewSampleRespVO previewSample;

    public List<String> getSendTypes() {
        return sendTypes;
    }

    public void setSendTypes(List<String> sendTypes) {
        this.sendTypes = sendTypes;
    }

    public List<String> getTemplateTypes() {
        return templateTypes;
    }

    public void setTemplateTypes(List<String> templateTypes) {
        this.templateTypes = templateTypes;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public Map<String, Integer> getValidityMinutes() {
        return validityMinutes;
    }

    public void setValidityMinutes(Map<String, Integer> validityMinutes) {
        this.validityMinutes = validityMinutes;
    }

    public MailTemplatePreviewSampleRespVO getPreviewSample() {
        return previewSample;
    }

    public void setPreviewSample(MailTemplatePreviewSampleRespVO previewSample) {
        this.previewSample = previewSample;
    }
}
