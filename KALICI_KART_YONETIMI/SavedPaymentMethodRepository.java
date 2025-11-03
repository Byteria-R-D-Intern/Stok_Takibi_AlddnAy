package com.example.demo.domain.port;

import java.util.List;
import java.util.Optional;

import com.example.demo.domain.model.SavedPaymentMethod;

// NEDEN: Kalıcı yöntemler için domain port
public interface SavedPaymentMethodRepository {
    SavedPaymentMethod save(SavedPaymentMethod m);
    List<SavedPaymentMethod> findByUserId(Long userId);
    Optional<SavedPaymentMethod> findByIdAndUserId(Long id, Long userId);
    void deactivate(Long id, Long userId);
    void delete(Long id, Long userId);
}


