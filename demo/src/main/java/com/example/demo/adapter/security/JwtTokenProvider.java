package com.example.demo.adapter.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.domain.port.TokenProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider implements TokenProvider {

	@Value("${JWT_SECRET_KEY}")
	private String secret;

	
	private long ttlMillis = 3600000;

	@Override
    public String createToken(Long userId, String role) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + ttlMillis);
		return Jwts.builder()
				.setSubject(String.valueOf(userId))
				.claim("role", role)
				.setIssuedAt(now)
				.setExpiration(exp)
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
				.compact();
	}

    public boolean validate(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

    public Long getUserId(String token) {
		Claims c = Jwts.parserBuilder()
				.setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
				.build()
				.parseClaimsJws(token)
				.getBody();
		return Long.valueOf(c.getSubject());
	}

	@Override
	public String getRole(String token) {
		Claims c = Jwts.parserBuilder()
				.setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
				.build()
				.parseClaimsJws(token)
				.getBody();
		return c.get("role", String.class);
	}
}


