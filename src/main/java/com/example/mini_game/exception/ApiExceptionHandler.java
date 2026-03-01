package com.example.mini_game.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
        org.springframework.dao.CannotAcquireLockException.class,
        org.springframework.orm.jpa.JpaSystemException.class
    })
    public ResponseEntity<?> handleLockTimeout(Exception ex) {
        return ResponseEntity.status(409).body(Map.of(
                "message", "Too many concurrent requests. Please try again."
        ));
    }
}