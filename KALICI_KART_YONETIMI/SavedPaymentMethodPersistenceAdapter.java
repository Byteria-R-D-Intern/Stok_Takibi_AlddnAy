package com.example.demo.adapter.persistence.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.SavedPaymentMethod;
import com.example.demo.domain.port.SavedPaymentMethodRepository;

// NEDEN: Port implementasyonu — domain port'un JPA ile köprüsü
@Repository
public class SavedPaymentMethodPersistenceAdapter implements SavedPaymentMethodRepository {

    private final SavedPaymentMethodJpaRepository jpa;

    public SavedPaymentMethodPersistenceAdapter(SavedPaymentMethodJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SavedPaymentMethod save(SavedPaymentMethod m) { return jpa.save(m); }

    @Override
    public List<SavedPaymentMethod> findByUserId(Long userId) { return jpa.findByUserIdOrderByCreatedAtDesc(userId); }

    @Override
    public Optional<SavedPaymentMethod> findByIdAndUserId(Long id, Long userId) { return jpa.findByIdAndUserId(id, userId); }

    @Override
    public void deactivate(Long id, Long userId) { jpa.deactivate(id, userId); }

    @Override
    public void delete(Long id, Long userId) { jpa.deleteByIdAndUserId(id, userId); }
}


