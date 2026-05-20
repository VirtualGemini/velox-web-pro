package com.velox.module.system.id.support;

public record DatabaseIdColumnBinding(
        String columnName,
        String targetBusinessType,
        DatabaseIdReferenceKind kind,
        boolean pathLike
) {
}
