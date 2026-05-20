package com.velox.module.system.id.support;

public enum DatabaseIdReferenceKind {

    EXPLICIT_REFERENCE,
    EXPLICIT_FK,
    SHARED_PRIMARY_KEY,
    SELF_REFERENCE,
    AUDIT_REFERENCE,
    PATH_REFERENCE
}
