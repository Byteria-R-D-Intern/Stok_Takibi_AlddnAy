package com.example.demo.application.service.tokenization;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// NEDEN: PAN+SKT verisini kalıcı saklamak için AES-GCM ile mühürleme/açma yardımcı sınıfı
// Not: Üretimde anahtar yönetimi KMS/HSM ile yapılmalıdır.
public class CardSealingService {

    private final SecretKey secretKey;
    private final SecureRandom random = new SecureRandom();

    // NEDEN: Dışarıdan 16/24/32 byte key alınır (ör. Base64 çözülmüş hali)
    public CardSealingService(byte[] keyBytes) {
        if (keyBytes == null || !(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("invalid_key_length");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    // NEDEN: PAN+SKT'yi "pan|mm|yyyy" olarak paketleyip AES-GCM ile şifreler
    public String seal(String pan, int expMonth, int expYear) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);

            String payload = pan + "|" + expMonth + "|" + expYear;
            byte[] plain = payload.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
            byte[] cipherBytes = cipher.doFinal(plain);

            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String cB64 = Base64.getEncoder().encodeToString(cipherBytes);
            return ivB64 + ":" + cB64;
        } catch (Exception e) {
            throw new IllegalStateException("seal_error", e);
        }
    }

    // NEDEN: "iv:cipher" formatındaki metni çözerek PAN+SKT'yi geri verir
    public DetokenizeResult unseal(String sealed) {
        try {
            String[] parts = sealed.split(":", 2);
            if (parts.length != 2) throw new IllegalArgumentException("invalid_sealed_payload");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherBytes = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            String payload = new String(plain, StandardCharsets.UTF_8);

            String[] pp = payload.split("\\|");
            if (pp.length != 3) throw new IllegalArgumentException("invalid_sealed_payload");
            String pan = pp[0];
            int m = Integer.parseInt(pp[1]);
            int y = Integer.parseInt(pp[2]);
            return new DetokenizeResult(pan, m, y);
        } catch (Exception e) {
            throw new IllegalStateException("unseal_error", e);
        }
    }

    // NEDEN: Basit taşıyıcı
    public record DetokenizeResult(String pan, int expMonth, int expYear) {}
}


