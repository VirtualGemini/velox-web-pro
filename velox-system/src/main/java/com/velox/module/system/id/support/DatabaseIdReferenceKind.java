package com.velox.module.system.id.support;

public enum DatabaseIdReferenceKind {

    EXPLICIT_FK,
    SHARED_PRIMARY_KEY,
    SELF_REFERENCE,
    WEAK_REFERENCE,
    AUDIT_REFERENCE,
    PATH_REFERENCE
}
