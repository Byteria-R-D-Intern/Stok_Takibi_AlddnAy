package com.example.demo.application.service.tokenization;


import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenizationService {

    public record EncryptedCard(String cipherBase64, String ivBase64, int expMonth, int expYear, Instant expiresAt) {}

    private final Map<String, EncryptedCard> store = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration ttl;
    private final SecretKey secretKey;

    public TokenizationService(
            @Value("${token.ttl-seconds:600}") long ttlSeconds,
            @Value("${token.secret}") String base64Key
    ) {
        this.ttl = Duration.ofSeconds(ttlSeconds);
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public static boolean isValidLuhn(String pan) {
        int sum = 0; boolean alt = false;
        for (int i = pan.length() - 1; i >= 0; i--) {
            int n = pan.charAt(i) - '0';
            if (alt) { n *= 2; if (n > 9) n -= 9; }
            sum += n; alt = !alt;
        }
        return sum % 10 == 0;
    }

    public String tokenize(String pan, int expMonth, int expYear) {
        if (pan == null || pan.length() < 12 || !pan.chars().allMatch(Character::isDigit) || !isValidLuhn(pan))
            throw new IllegalArgumentException("invalid_pan");
        if (expMonth < 1 || expMonth > 12 || expYear < 2000) throw new IllegalArgumentException("invalid_expiry");

        String token = generateToken();
        EncryptedCard encrypted = encrypt(pan, expMonth, expYear);
        store.put(token, encrypted);
        return token;
    }

    public DetokenizeResult detokenize(String token) {
        EncryptedCard enc = store.get(token);
        if (enc == null) throw new IllegalArgumentException("token_not_found");
        if (enc.expiresAt.isBefore(Instant.now())) { store.remove(token); throw new IllegalStateException("token_expired"); }
        return decrypt(enc);
    }

    public void revoke(String token) { store.remove(token); }

    @Scheduled(fixedDelay = 60000)
    public void evictExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return "tok_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private EncryptedCard encrypt(String pan, int expMonth, int expYear) {
        try {
            byte[] iv = new byte[12]; random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
            byte[] plain = pan.getBytes(StandardCharsets.UTF_8);
            byte[] cipherBytes = cipher.doFinal(plain);
            Instant expiresAt = Instant.now().plus(ttl);
            return new EncryptedCard(Base64.getEncoder().encodeToString(cipherBytes), Base64.getEncoder().encodeToString(iv), expMonth, expYear, expiresAt);
        } catch (Exception e) { throw new IllegalStateException("encrypt_error", e); }
    }

    public record DetokenizeResult(String pan, int expMonth, int expYear) {}

    private DetokenizeResult decrypt(EncryptedCard enc) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = Base64.getDecoder().decode(enc.ivBase64());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
            byte[] cipherBytes = Base64.getDecoder().decode(enc.cipherBase64());
            String pan = new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
            return new DetokenizeResult(pan, enc.expMonth(), enc.expYear());
        } catch (Exception e) { throw new IllegalStateException("decrypt_error", e); }
    }
}


