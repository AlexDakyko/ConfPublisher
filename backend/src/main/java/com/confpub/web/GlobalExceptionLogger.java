package com.confpub.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Гарантированно логируем ЛЮБОЕ неперехваченное исключение с полным стеком.
 * Возвращаем тот же «Unexpected error»-формат, что и раньше, чтобы фронт не ломался.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionLogger {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAnyException(Exception ex) {
        // 1) Печатаем стек в консоль IntelliJ (RUN) — всегда
        log.error("Unhandled exception", ex);

        // 2) Возвращаем JSON так же, как делал твой GlobalExceptionHandler
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Unexpected error");
        // По желанию можно добавить путь запроса, если есть доступ к HttpServletRequest.
        // body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}