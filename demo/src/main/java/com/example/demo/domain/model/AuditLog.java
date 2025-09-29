package com.example.demo.domain.model;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id")
	private Long id;

	@Column(name = "actor_user_id", nullable = false)
	private Long actorUserId;

	@Column(name = "target_type", nullable = false)
	private String targetType; // 'order','product','user','comment','shipment',...

	@Column(name = "target_id", nullable = false)
	private Long targetId;

	@Column(name = "action", nullable = false)
	private String action; // 'create','update','delete','login','register',...

	@Column(name = "message")
	private String message;

	@JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes_json", columnDefinition = "jsonb")
    private String changesJson; 

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
}


