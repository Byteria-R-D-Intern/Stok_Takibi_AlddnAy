package com.example.demo.domain.port;

public interface TokenProvider {
    String createToken(Long userId, String role);
    boolean validate(String token);
    Long getUserId(String token);
    String getRole(String token);
}


