package com.example.demo.adapter.web.common;


import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.example.demo.domain.model.IdempotencyKey;
import com.example.demo.domain.port.IdempotencyKeyRepository;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class IdempotencyFilter implements Filter {

    private final IdempotencyKeyRepository repository;
    private final Duration ttl;

    public IdempotencyFilter(IdempotencyKeyRepository repository,
                             @Value("${idempotency.ttl-seconds:900}") long ttlSeconds) {
        this.repository = repository;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        // Only apply to POST on payments endpoints
        String path = httpReq.getRequestURI();
        if (!"POST".equalsIgnoreCase(httpReq.getMethod()) || !path.startsWith("/api/payments")) {
            chain.doFilter(request, response);
            return;
        }

        String idempotencyKey = httpReq.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            httpRes.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Idempotency-Key header");
            return;
        }

        byte[] bodyBytes = StreamUtils.copyToByteArray(httpReq.getInputStream());
        String requestHash = HashUtils.sha256Hex(httpReq.getMethod() + "|" + path + "|" + new String(bodyBytes));
        CachedBodyHttpServletRequest cachedReq = new CachedBodyHttpServletRequest(httpReq, bodyBytes);

        Optional<IdempotencyKey> existing = repository.findByKeyAndEndpoint(idempotencyKey, path);
        if (existing.isPresent()) {
            IdempotencyKey key = existing.get();
            if (!key.getRequestHash().equals(requestHash)) {
                httpRes.sendError(HttpServletResponse.SC_CONFLICT, "Idempotency-Key mismatch for different request");
                return;
            }
            if (key.getHttpStatus() != null && key.getResponseBody() != null) {
                httpRes.setStatus(key.getHttpStatus());
                httpRes.getWriter().write(key.getResponseBody());
                httpRes.setHeader("Idempotent-Replayed", "true");
                return;
            }
        }

        ResponseCaptureWrapper responseWrapper = new ResponseCaptureWrapper(httpRes);
        chain.doFilter(cachedReq, responseWrapper);

        int status = responseWrapper.getStatus();
        String responseBody = new String(responseWrapper.getCapturedAsBytes());

        IdempotencyKey record = new IdempotencyKey(null, idempotencyKey, path, requestHash,
                status, responseBody, Instant.now(), Instant.now().plus(ttl));
        repository.save(record);

        httpRes.setStatus(status);
        httpRes.getOutputStream().write(responseWrapper.getCapturedAsBytes());
    }
}


