package com.example.demo.adapter.persistence.idempotency;


import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.IdempotencyKey;
import com.example.demo.domain.port.IdempotencyKeyRepository;

@Repository
public class IdempotencyKeyPersistenceAdapter implements IdempotencyKeyRepository {

    private final IdempotencyKeyJpaRepository jpa;

    public IdempotencyKeyPersistenceAdapter(IdempotencyKeyJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public IdempotencyKey save(IdempotencyKey key) { return jpa.save(key); }

    @Override
    public Optional<IdempotencyKey> findByKeyAndEndpoint(String keyValue, String endpoint) {
        return jpa.findByKeyValueAndEndpoint(keyValue, endpoint);
    }

    @Override
    public void deleteExpired() { jpa.deleteExpired(Instant.now()); }
}


