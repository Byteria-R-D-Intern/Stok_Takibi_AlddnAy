package com.example.demo.adapter.web.tokenization;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.service.tokenization.TokenizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api")
@Tag(name = "Public Tokenization", description = "Kullanıcıların erişebildiği tokenizasyon uçları")
public class PublicTokenizationController {

    private final TokenizationService svc;

    public PublicTokenizationController(TokenizationService svc) { this.svc = svc; }

    public static record TokenizeRequest(@NotBlank String pan, @NotNull Integer expMonth, @NotNull Integer expYear) {}
    public static record TokenizeResponse(String token, String last4, String brand, Integer expMonth, Integer expYear) {}

    @PostMapping("/tokenize")
    @Operation(summary = "PAN'ı tokenize et (public)", description = "Geçerli PAN/SKT alır, şifreli saklar ve token döner. JWT yeterli; admin rolü gerekmez.")
    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody TokenizeRequest req) {
        String token = svc.tokenize(req.pan(), req.expMonth(), req.expYear());
        String last4 = req.pan().substring(Math.max(0, req.pan().length() - 4));
        String brand = detectBrand(req.pan());
        return ResponseEntity.ok(new TokenizeResponse(token, last4, brand, req.expMonth(), req.expYear()));
    }

    private static String detectBrand(String pan) {
        if (pan.startsWith("4")) return "VISA";
        if (pan.startsWith("5")) return "MASTERCARD";
        if (pan.startsWith("3")) return "AMEX";
        return "CARD";
    }
}


