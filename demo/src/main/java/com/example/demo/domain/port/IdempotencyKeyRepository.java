package com.example.demo.domain.port;


import java.util.Optional;

import com.example.demo.domain.model.IdempotencyKey;

public interface IdempotencyKeyRepository {

    IdempotencyKey save(IdempotencyKey key);

    Optional<IdempotencyKey> findByKeyAndEndpoint(String keyValue, String endpoint);

    void deleteExpired();
}


