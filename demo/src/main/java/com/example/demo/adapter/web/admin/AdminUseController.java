package com.example.demo.adapter.web.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.application.usecase.UserUseCase;
import com.example.demo.domain.model.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users", description = "Yönetici kullanıcı işlemleri")
public class AdminUseController {

    private final UserUseCase userUseCase;

    public AdminUseController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Kullanıcıları Listele", description = "Tüm kullanıcıları döner")
    public ResponseEntity<List<User>> listAll(@RequestParam(name = "active", required = false) Boolean active) {
        return ResponseEntity.ok(userUseCase.listAllUsers(active));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/active")
    @Operation(summary = "Kullanıcıyı Aktif/Pasif Yap", description = "Kullanıcıyı aktif/pasif yapır")
    public ResponseEntity<Void> setActive(@PathVariable Long id, @RequestBody Boolean active) {
        userUseCase.setActive(id, active);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Kullanıcıyı pasifleştir")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userUseCase.setActive(id, false);
        return ResponseEntity.noContent().build();
    }
    
    
}
