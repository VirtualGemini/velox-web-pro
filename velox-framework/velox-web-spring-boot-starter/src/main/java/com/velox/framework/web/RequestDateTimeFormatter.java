package com.velox.framework.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class RequestDateTimeFormatter {

    private RequestDateTimeFormatter() {
    }

    public static String format(LocalDateTime utcDateTime) {
        return com.velox.framework.web.api.timezone.RequestDateTimeFormatter.format(utcDateTime);
    }

    public static LocalDateTime toUtcStartOfDay(LocalDate localDate) {
        return com.velox.framework.web.api.timezone.RequestDateTimeFormatter.toUtcStartOfDay(localDate);
    }

    public static LocalDateTime toUtcEndOfDay(LocalDate localDate) {
        return com.velox.framework.web.api.timezone.RequestDateTimeFormatter.toUtcEndOfDay(localDate);
    }
}
