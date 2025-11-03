package com.example.demo.adapter.web.auth;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.application.usecase.UserUseCase;
import com.example.demo.application.usecase.AuditLogUseCase;
import com.example.demo.domain.port.TokenProvider;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Kimlik doğrulama işlemleri")
public class AuthController {

    private final UserUseCase userUseCase;
    private final AuditLogUseCase auditLogUseCase;
    private final TokenProvider tokenProvider;

    public AuthController(UserUseCase userUseCase, AuditLogUseCase auditLogUseCase, TokenProvider tokenProvider) {
        this.userUseCase = userUseCase;
        this.auditLogUseCase = auditLogUseCase;
        this.tokenProvider = tokenProvider;
	}

    @PostMapping("/login")
    @Operation(summary = "Giriş yap", description = "Email/şifre ile giriş ve JWT üretimi")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        // NEDEN: Pasif kullanıcıların girişini 403 ile engelle
        var uOpt = userUseCase.getByEmail(req.email);
        if (uOpt.isPresent() && !uOpt.get().isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    
        Optional<String> token = userUseCase.login(req.email, req.password);
    
        return token.map(t -> {
                    Long actorId = null;
                    try { actorId = tokenProvider.getUserId(t); } catch (Exception ignored) {}
                    auditLogUseCase.log(actorId, "user", actorId, "login", "successful login", null);
                    return ResponseEntity.ok(new TokenResponse(t));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/register")
    @Operation(summary = "Kayıt ol", description = "Yeni kullanıcı oluştur ve JWT döndür")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest req) {
		Optional<String> token = userUseCase.register(
			req.email,
			req.firstName,
			req.lastName,
			req.password,
			req.role
		);
        return token.map(t -> {
                    // register sonrası dönen token’dan yeni kullanıcının id’sini okuyup actor/target olarak set ediyoruz
                    Long newUserId = null;
                    try { newUserId = tokenProvider.getUserId(t); } catch (Exception ignored) {}
                    auditLogUseCase.log(newUserId, "user", newUserId, "register", "successful register", null);
                    return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(t));
                })
					.orElseGet(() -> ResponseEntity.badRequest().build());
	}

    

	// DTOlar
    public static class LoginRequest {
        @Schema(description = "Email")
        @Email @NotBlank public String email;
        @Schema(description = "Şifre")
        @NotBlank public String password;
    }

    public static class RegisterRequest {
        @Schema(description = "Email")
        @Email @NotBlank public String email;
        @Schema(description = "Ad")
        @NotBlank public String firstName;
        @Schema(description = "Soyad")
        @NotBlank public String lastName;
        @Schema(description = "Şifre")
        @NotBlank public String password;
        @Schema(description = "Rol (opsiyonel)")
        public String role; // default CUSTOMER
    }

	public static class TokenResponse {
		public final String token;
		public TokenResponse(String token) { this.token = token; }
	}
}


