package com.example.demo.domain.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

// NEDEN: Kullanıcıya ait kalıcı kart kaydı (brand/last4/label + mühürlü kart)
@Data
@Entity
@Table(name = "saved_payment_methods", indexes = {
    @Index(name = "idx_spm_user", columnList = "user_id"),
    @Index(name = "idx_spm_user_active", columnList = "user_id, active")
})
public class SavedPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spm_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "brand", length = 20)
    private String brand;

    @Column(name = "last4", length = 4)
    private String last4;

    @Column(name = "label", length = 80)
    private String label;

    @Lob
    @Column(name = "sealed_card", nullable = false)
    private String sealedCard;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}


