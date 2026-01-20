package io.github.joaovitorleal.securecapita.exception.handler;

import io.github.joaovitorleal.securecapita.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        log.error("Validation error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ex.getBody();
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail("Validation failed for one or more fields.");

        List<Map<String, String>> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put("message", error.getDefaultMessage());
                    return errorMap;
                })
                .collect(Collectors.toList());

        problemDetail.setProperty("errors", validationErrors);
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(statusCode).body(problemDetail);
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(final ApiException ex, final HttpServletRequest request) {
        log.error("[ApiException] Internal server error at: [{}]: {} - {}",
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );
        return this.createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR_MESSAGE,
                "Internal Server Error",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUntreatedException(final Exception ex, final HttpServletRequest request) {
        log.error("Unexpected internal server error at: [{}]: {} - {}",
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );
        return this.createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR_MESSAGE,
                "Internal Server Error",
                request
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExistsException(final EmailAlreadyExistsException ex, final HttpServletRequest request) {
        return this.createProblemDetail(HttpStatus.CONFLICT, ex.getMessage(), "Email Already Exists", request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(final ResourceNotFoundException ex, final HttpServletRequest request) {
        return this.createProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), "Resource Not Found", request);
    }

    @ExceptionHandler(NotificationFailureException.class)
    public ProblemDetail handleNotificationFailureException(final NotificationFailureException ex, final HttpServletRequest request) {
        return this.createProblemDetail(HttpStatus.BAD_GATEWAY, ex.getMessage(), "Message Delivery Failed", request);
    }

    @ExceptionHandler({
            MfaCodeInvalidException.class,
            MfaCodeExpiredException.class,
            MfaVerificationNotFoundByUserIdException.class
    })
    public ProblemDetail handleMfaAuthenticationFailureException(final Exception ex, final HttpServletRequest request) {
        log.error("MFA Authentication failure: class: {}, message: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return this.createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed.",
                "Authentication Failed",
                request
        );
    }

    private ProblemDetail createProblemDetail(final HttpStatus status, final String detail, final String title, final HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
