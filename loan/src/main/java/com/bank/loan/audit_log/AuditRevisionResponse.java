package com.bank.loan.audit_log;

public record AuditRevisionResponse<T>(
        Long revisionNumber,
        String timestamp,
        String revisionType,
        String entityName,
        Long entityId,
        T snapshot
) {
}
