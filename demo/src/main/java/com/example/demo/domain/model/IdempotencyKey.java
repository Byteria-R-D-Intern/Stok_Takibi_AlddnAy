package com.example.demo.domain.model;


import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "idempotency_keys",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"key_value", "endpoint"})
    },
    indexes = {
        @Index(name = "idx_idem_key_endpoint", columnList = "key_value, endpoint"),
        @Index(name = "idx_idem_expires_at", columnList = "expires_at")
    }
)
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "key_value", nullable = false, length = 100)
    private String keyValue;

    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Lob
    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}


