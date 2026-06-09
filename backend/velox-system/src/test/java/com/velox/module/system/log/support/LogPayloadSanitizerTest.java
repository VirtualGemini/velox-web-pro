package com.velox.module.system.log.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velox.module.system.log.config.SystemLogProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogPayloadSanitizerTest {

    private final LogPayloadSanitizer sanitizer = new LogPayloadSanitizer(
            new ObjectMapper(),
            new SystemLogProperties()
    );

    @Test
    void shouldMaskSensitiveFieldsFromEnumInNestedJson() {
        String payload = """
                {
                  "mfaChallenge": "challenge-value",
                  "captcha_code_key": "captcha-key",
                  "items": [
                    {
                      "new-email-code": "mail-secret",
                      "normal": "visible"
                    }
                  ]
                }
                """;

        String sanitized = sanitizer.sanitizeJsonOrText(payload, 1000);

        assertThat(sanitized)
                .contains("\"mfaChallenge\":\"******\"")
                .contains("\"captcha_code_key\":\"******\"")
                .contains("\"new-email-code\":\"******\"")
                .contains("\"normal\":\"visible\"")
                .doesNotContain("challenge-value")
                .doesNotContain("captcha-key")
                .doesNotContain("mail-secret");
    }

    @Test
    void shouldMaskSensitiveFieldsFromEnumInText() {
        String sanitized = sanitizer.sanitizeJsonOrText(
                "mfaChallenge=challenge-value token:access-token normal=visible",
                1000
        );

        assertThat(sanitized)
                .contains("mfaChallenge=******")
                .contains("token=******")
                .contains("normal=visible")
                .doesNotContain("challenge-value")
                .doesNotContain("access-token");
    }
}
