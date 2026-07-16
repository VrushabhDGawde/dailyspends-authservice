package com.spendsense.backend.common.exception;

import com.spendsense.backend.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private ErrorResponse buildErrorResponse(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request) {

                return ErrorResponse.builder()
                                .timestamp(Instant.now())
                                .status(status.value())
                                .error(status.getReasonPhrase())
                                .message(message)
                                .path(request.getRequestURI())
                                .build();
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                FieldError fieldError = exception.getBindingResult().getFieldError();

                String message = fieldError != null
                                ? fieldError.getDefaultMessage()
                                : "Validation failed";

                return ResponseEntity.badRequest()
                                .body(buildErrorResponse(
                                                HttpStatus.BAD_REQUEST,
                                                message,
                                                request));
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolationException(
                        ConstraintViolationException exception,
                        HttpServletRequest request) {

                return ResponseEntity.badRequest()
                                .body(buildErrorResponse(
                                                HttpStatus.BAD_REQUEST,
                                                exception.getMessage(),
                                                request));
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
                        EmailAlreadyExistsException exception,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(buildErrorResponse(
                                                HttpStatus.CONFLICT,
                                                exception.getMessage(),
                                                request));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException exception,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(buildErrorResponse(
                                                HttpStatus.UNAUTHORIZED,
                                                "Invalid email or password",
                                                request));
        }

        @ExceptionHandler(InvalidRefreshTokenException.class)
        public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(
                        InvalidRefreshTokenException exception,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(buildErrorResponse(
                                                HttpStatus.UNAUTHORIZED,
                                                exception.getMessage(),
                                                request));
        }

        @ExceptionHandler(TokenExpiredException.class)
        public ResponseEntity<ErrorResponse> handleTokenExpiredException(
                        TokenExpiredException exception,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(buildErrorResponse(
                                                HttpStatus.UNAUTHORIZED,
                                                exception.getMessage(),
                                                request));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleException(
                        Exception exception,
                        HttpServletRequest request) {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(buildErrorResponse(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Something went wrong",
                                                request));
        }
}