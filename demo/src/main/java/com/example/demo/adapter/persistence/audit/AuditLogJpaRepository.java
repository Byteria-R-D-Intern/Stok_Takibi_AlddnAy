package com.example.demo.adapter.persistence.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.AuditLog;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long> {}


