package com.velox.module.system.id.support;

import java.util.List;

public record DatabaseIdManagedTable(
        String tableName,
        String identityColumn,
        String identityMappingBusinessType,
        List<DatabaseIdColumnBinding> bindings
) {
}
