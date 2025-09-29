package com.example.demo.domain.port;

import com.example.demo.domain.model.AuditLog;

public interface AuditLogRepository {
	AuditLog save(AuditLog log);
}


