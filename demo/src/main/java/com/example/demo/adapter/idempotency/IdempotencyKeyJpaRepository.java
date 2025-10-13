package com.example.demo.adapter.idempotency;


import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.model.IdempotencyKey;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByKeyValueAndEndpoint(String keyValue, String endpoint);

    @Transactional
    @Modifying
    @Query("delete from IdempotencyKey k where k.expiresAt < ?1")
    int deleteExpired(Instant now);
}


