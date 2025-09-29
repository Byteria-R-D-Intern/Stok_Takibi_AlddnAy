package com.example.demo.adapter.persistence.audit;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.AuditLog;
import com.example.demo.domain.port.AuditLogRepository;

@Repository
public class AuditLogPersistenceAdapter implements AuditLogRepository {

	private final AuditLogJpaRepository jpa;

	public AuditLogPersistenceAdapter(AuditLogJpaRepository jpa) { this.jpa = jpa; }

	@Override
	public AuditLog save(AuditLog log) { return jpa.save(log); }
}


