package com.coopgoal.audit.service;

import com.coopgoal.audit.domain.AuditLog;
import com.coopgoal.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) { this.repository = repository; }

    public void record(UUID userId, String entityType, UUID entityId, String action, String details) {
        repository.save(AuditLog.create(userId, entityType, entityId, action, details));
    }
}
