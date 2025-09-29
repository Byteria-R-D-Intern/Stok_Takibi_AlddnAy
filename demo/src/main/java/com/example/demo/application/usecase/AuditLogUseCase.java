package com.example.demo.application.usecase;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.demo.domain.model.AuditLog;
import com.example.demo.domain.port.AuditLogRepository;

@Service
public class AuditLogUseCase {

	private final AuditLogRepository auditLogRepository;

	public AuditLogUseCase(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	public void log(Long actorUserId, String targetType, Long targetId, String action, String message, String changesJson) {
		AuditLog log = new AuditLog();
		log.setActorUserId(actorUserId);
		log.setTargetType(targetType);
		log.setTargetId(targetId);
		log.setAction(action);
		log.setMessage(message);
		log.setChangesJson(changesJson);
		log.setCreatedAt(Instant.now());
		auditLogRepository.save(log);
	}
}


