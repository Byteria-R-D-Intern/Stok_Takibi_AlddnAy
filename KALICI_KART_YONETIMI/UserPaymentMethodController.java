package com.example.demo.adapter.web.payment;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.application.service.tokenization.TokenizationService;
import com.example.demo.domain.model.SavedPaymentMethod;
import com.example.demo.domain.port.SavedPaymentMethodRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// NEDEN: Kullanıcının kayıtlı ödeme yöntemlerini yönetmek için REST uçları
@RestController
@RequestMapping("/api/payment-methods")
@Tag(name = "Payment Methods", description = "Kullanıcının kayıtlı ödeme yöntemleri")
public class UserPaymentMethodController {

    private final SavedPaymentMethodRepository repo;
    private final TokenizationService tokens;

    public UserPaymentMethodController(SavedPaymentMethodRepository repo, TokenizationService tokens) {
        this.repo = repo;
        this.tokens = tokens;
    }

    @GetMapping
    @Operation(summary = "Listele")
    public ResponseEntity<List<SavedPaymentMethod>> list(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(repo.findByUserId(userId));
    }

    public static class SaveRequest {
        public String token;  // /api/tokenize ile gelen tek-kullanımlık token
        public String label;  // opsiyonel kullanıcı etiketi
    }

    public static class SaveResponse { public final Long id; public SaveResponse(Long id){ this.id = id; } }

    @PostMapping
    @Operation(summary = "Yeni yöntem kaydet", description = "Tek-kullanımlık token'ı kalıcı olarak mühürleyip kaydeder")
    public ResponseEntity<SaveResponse> save(Authentication auth, @RequestBody SaveRequest req) {
        Long userId = Long.valueOf(auth.getName());

        var d = tokens.detokenize(req.token);
        String brand = detectBrand(d.pan());
        String last4 = d.pan().substring(d.pan().length() - 4);

        String sealed = tokens.seal(d.pan(), d.expMonth(), d.expYear());
        tokens.revoke(req.token);

        SavedPaymentMethod m = new SavedPaymentMethod();
        m.setUserId(userId);
        m.setBrand(brand);
        m.setLast4(last4);
        m.setLabel(req.label);
        m.setSealedCard(sealed);
        m.setActive(true);
        m.setCreatedAt(Instant.now());
        m = repo.save(m);

        return ResponseEntity.ok(new SaveResponse(m.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Sil")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        repo.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Pasifleştir")
    public ResponseEntity<Void> deactivate(Authentication auth, @PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        repo.deactivate(id, userId);
        return ResponseEntity.noContent().build();
    }

    private String detectBrand(String pan) {
        if (pan == null || pan.isBlank()) return "CARD";
        if (pan.startsWith("4")) return "VISA";
        if (pan.startsWith("5")) return "MASTERCARD";
        if (pan.startsWith("3")) return "AMEX";
        return "CARD";
    }
}


