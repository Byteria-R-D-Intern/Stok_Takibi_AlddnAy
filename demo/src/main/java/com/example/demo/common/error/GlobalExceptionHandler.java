package com.example.demo.common.error;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	
	private ResponseEntity<ApiError> build(HttpStatus status, String message, String path, List<String> details) {
		ApiError body = new ApiError(status.value(), status.getReasonPhrase(), message, path);
		if (details != null) body.details.addAll(details);
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		List<String> details = ex.getBindingResult().getFieldErrors()
			.stream().map(this::formatField).collect(Collectors.toList());
		return build(HttpStatus.BAD_REQUEST, "Alan doğrulaması başarısız", req.getRequestURI(), details);
	}

	@ExceptionHandler({ MethodArgumentTypeMismatchException.class, HttpMediaTypeNotSupportedException.class, IllegalArgumentException.class })
	public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, emptyIfNull(ex.getMessage()), req.getRequestURI(), null);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
		return build(HttpStatus.FORBIDDEN, "Erişim reddedildi", req.getRequestURI(), null);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, emptyIfNull(ex.getMessage()), req.getRequestURI(), null);
	}

	@ExceptionHandler({ IllegalStateException.class, DataIntegrityViolationException.class })
	public ResponseEntity<ApiError> handleConflict(Exception ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, emptyIfNull(ex.getMessage()), req.getRequestURI(), null);
	}

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleRSE(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(status, emptyIfNull(ex.getReason()), req.getRequestURI(), null);
    }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu", req.getRequestURI(), null);
	}

	private String formatField(FieldError fe) {
		return fe.getField() + ": " + fe.getDefaultMessage();
	}

	private String emptyIfNull(String s) { return s == null ? "" : s; }
}


