package com.example.demo.application.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {
    private final int maxAttempts = 5;
    private final Duration lockDuration = Duration.ofMinutes(5);
  
    private static class Attempt {
      int failures = 0;
      Instant lockUntil = null;
    }
  
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();
  
    public boolean isBlocked(String key) {
      Attempt a = attempts.get(keyKey(key));
      return a != null && a.lockUntil != null && Instant.now().isBefore(a.lockUntil);
    }
  
    public void onFailure(String key) {
      String k = keyKey(key);
      Attempt a = attempts.computeIfAbsent(k, kk -> new Attempt());
      if (a.lockUntil != null && Instant.now().isBefore(a.lockUntil)) return;
      a.failures++;
      if (a.failures >= maxAttempts) {
        a.lockUntil = Instant.now().plus(lockDuration);
        a.failures = 0; // sayacı sıfırla; kilit süresince bloklu kalacak
      }
    }
  
    public void onSuccess(String key) {
      attempts.remove(keyKey(key));
    }
  
    private String keyKey(String key) {
      return key == null ? "null" : key.trim().toLowerCase();
    }
  }
