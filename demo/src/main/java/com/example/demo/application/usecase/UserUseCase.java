package com.example.demo.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.domain.model.Role;
import com.example.demo.domain.model.User;
import com.example.demo.domain.port.PasswordHasher;
import com.example.demo.domain.port.TokenProvider;
import com.example.demo.domain.port.UserRepository;

import jakarta.transaction.Transactional;

import com.example.demo.application.security.LoginRateLimiter;

@Service
public class UserUseCase {

	private static final Logger log = LoggerFactory.getLogger(UserUseCase.class);

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final LoginRateLimiter loginRateLimiter;

    public UserUseCase(UserRepository userRepository,
                      PasswordHasher passwordHasher,
                      TokenProvider tokenProvider,
                      LoginRateLimiter loginRateLimiter) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.loginRateLimiter = loginRateLimiter;
    }

    public Optional<String> login(String email, String password) {
        log.info("Kullanıcı giriş isteği: email={}", email);
        String key = email == null ? null : email.trim().toLowerCase();
        if (loginRateLimiter.isBlocked(key)) {
            return Optional.empty();
        }
        Optional<User> result = userRepository.findByEmail(key)
            .filter(u -> passwordHasher.matches(password, u.getHashedPassword()));
        if (result.isPresent()) {
            loginRateLimiter.onSuccess(key);
            User u = result.get();
            return Optional.of(tokenProvider.createToken(u.getId(), u.getRole().name()));
        } else {
            loginRateLimiter.onFailure(key);
            return Optional.empty();
        }
    }

	public Optional<User> getByEmail(String email) {
		if (email == null) return java.util.Optional.empty();
		String key = email.trim().toLowerCase(); // NEDEN: login akışındaki normalizasyon ile aynı olsun
		return userRepository.findByEmail(key);
	}

	public Optional<String> register(String email,
	                                String firstName,
	                                String lastName,
	                                String password,
	                                String roleStr) {
		if (!isValidEmail(email)) {
			log.warn("Geçersiz email formatı: {}", email);
			return Optional.empty();
		}
		if (!isValidPassword(password)) {
			log.warn("Geçersiz şifre formatı");
			return Optional.empty();
		}
		if (isBlank(firstName) || isBlank(lastName)) {
			log.warn("Ad veya soyad boş olamaz");
			return Optional.empty();
		}
		if (userRepository.existsByEmail(email)) {
			log.warn("Email zaten kullanımda: {}", email);
			return Optional.empty();
		}

		Role role;
		try { role = Role.valueOf(roleStr == null ? "CUSTOMER" : roleStr.toUpperCase()); }
		catch (IllegalArgumentException ex) { role = Role.CUSTOMER; }

		User u = new User();
		u.setFirstName(firstName);
		u.setLastName(lastName);
		u.setEmail(email);
		u.setRole(role);
		u.setHashedPassword(passwordHasher.hash(password));
		u.setCreatedAt(Instant.now());

		userRepository.save(u);
		String token = tokenProvider.createToken(u.getId(), u.getRole().name());
		log.info("Kullanıcı başarıyla kaydedildi: userId={}", u.getId());
		return Optional.of(token);
	}


	public List<User> listAllUsers(Boolean active) {

		List<User> all =userRepository.findAll();
		if(active == null) return all;

		return all.stream().filter(u -> u.isActive() == active.booleanValue()).collect(Collectors.toList());
	}

	//Kullanıcıyı aktif/pasif yapma 
	@Transactional//şimdilik böyle kalsın sonra düzeltilecek
	public void setActive(Long userId, boolean active) {
		User u = userRepository.findById(userId).orElseThrow(java.util.NoSuchElementException::new);
		u.setActive(active);
		userRepository.save(u);
	}
	
	private static boolean isBlank(String s) { return s == null || s.isBlank(); }
	private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	private static boolean isValidEmail(String email) { return email != null && EMAIL.matcher(email).matches(); }
	private static boolean isValidPassword(String pw) { return pw != null && pw.length() >= 6; }
}


