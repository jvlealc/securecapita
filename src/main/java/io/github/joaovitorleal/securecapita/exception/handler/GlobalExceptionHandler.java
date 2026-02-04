package io.github.joaovitorleal.securecapita.exception.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.github.joaovitorleal.securecapita.exception.*;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
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

    private static final String INTERNAL_SERVER_ERROR_TITLE = "Internal Server Error";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";
    private static final String AUTHENTICATION_ERROR_TITLE = "Authentication Failed";
    private static final String AUTHENTICATION_ERROR_MESSAGE = "Authentication failed.";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @Nonnull HttpHeaders headers,
            @Nonnull HttpStatusCode status,
            @Nonnull WebRequest request
    ) {
        log.warn("Validation error at [{}]: {}", ((ServletWebRequest) request).getRequest().getRequestURI(), ex.getMessage());

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

        return ResponseEntity.status(status).body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @Nonnull HttpMessageNotReadableException ex,
            @Nonnull HttpHeaders headers,
            @Nonnull HttpStatusCode status,
            @Nonnull WebRequest request
    ) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();

        log.warn("Malformed JSON at: [{}]: {}", servletRequest.getRequestURI(), ex.getMessage(), ex);

        String message = "Malformed JSON request or invalid field type.";
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException.getPath()
                    .stream()
                    .map(JsonMappingException.Reference::getFieldName) // nome do campo no JSON
                    .reduce((previous, current) -> previous + "." + current) // concatena níveis, se houver campos aninhados
                    .orElse("unknown");

            message += " Field: " + fieldName;
        }
        return ResponseEntity
                .status(status)
                .body(this.createProblemDetail(
                        HttpStatus.BAD_REQUEST,
                        message,
                        "Malformed JSON",
                        servletRequest
                ));
    }

    @Override
    public ResponseEntity<Object> handleNoHandlerFoundException(
            @Nonnull NoHandlerFoundException ex,
            @Nonnull HttpHeaders headers,
            @Nonnull HttpStatusCode status,
            @Nonnull WebRequest request
    ) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        log.warn("Endpoint [{}] not found for this method {}", servletRequest.getRequestURI(), ex.getHttpMethod());
        return ResponseEntity
                .status(status)
                .body(this.createProblemDetail(
                        HttpStatus.NOT_FOUND,
                        "The URI " + servletRequest.getRequestURI() + " does not exist on server.",
                        "Resource Not Found",
                        servletRequest
                ));
    }

    @Override
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            @Nonnull HttpRequestMethodNotSupportedException ex,
            @Nonnull HttpHeaders headers,
            @Nonnull HttpStatusCode status,
            @Nonnull WebRequest request
    ) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        log.warn("Method not supported {} at [{}]", ex.getMethod(), servletRequest.getRequestURI());
        return ResponseEntity
                .status(status)
                .body(this.createProblemDetail(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Method " + ex.getMethod() + " is not allowed on this resource.",
                        "Method Not Allowed",
                        servletRequest
                ));
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(final ApiException ex, final HttpServletRequest request) {
        log.error("[ApiException] Internal server error at URI [{}]: {} - {}",
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );
        return this.createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE, INTERNAL_SERVER_ERROR_TITLE, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUntreatedException(final Exception ex, final HttpServletRequest request) {
        log.error("Unexpected internal server error at URI [{}]: {} - {}",
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );
        return this.createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE, INTERNAL_SERVER_ERROR_TITLE, request);
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
        log.error("Notification failure: {}", ex.getMessage());
        return this.createProblemDetail(HttpStatus.BAD_GATEWAY, ex.getMessage(), "Message Delivery Failed", request);
    }

    @ExceptionHandler({
            PasswordMismatchException.class,
            ResetPasswordVerificationInvalidException.class,
            ResetPasswordVerificationExpiredException.class,
            AuthenticatedPasswordIncorrectException.class
    })
    public ProblemDetail handlePasswordBusinessException(final ApiException ex, final HttpServletRequest request) {
        log.warn("Password redefinition failure. Specific class: {} : {}", ex.getClass().getSimpleName(), ex.getMessage());
        String title = switch (ex) {
            case ResetPasswordVerificationInvalidException e -> "Reset Password Link Invalid";
            case ResetPasswordVerificationExpiredException e ->  "Reset Password Link Expired";
            case PasswordMismatchException e ->  "Password Mismatch";
            case AuthenticatedPasswordIncorrectException e -> "Password Incorrect";
            default -> "Password Validation Error";
        };
        return this.createProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), title, request);
    }

    @ExceptionHandler({
            MfaCodeInvalidException.class,
            MfaCodeExpiredException.class,
            MfaVerificationNotFoundByUserIdException.class
    })
    public ProblemDetail handleMfaAuthenticationFailureException(final Exception ex, final HttpServletRequest request) {
        log.warn("MFA Authentication failure: class: {}, for URI [{}]: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                ex.getMessage()
        );
        return this.createProblemDetail(HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR_MESSAGE, AUTHENTICATION_ERROR_TITLE, request);
    }

    /**
     * Handler centralizado para exceções JWT.
     */
    @ExceptionHandler(JwtAuthenticationInvalidException.class)
    public ProblemDetail handleJwtAuthenticationInvalidException(final JwtAuthenticationInvalidException ex, final HttpServletRequest request) {
        log.warn("JWT authentication failed at URI [{}]: {}", request.getRequestURI(), ex.getMessage());
        return this.createProblemDetail(HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR_MESSAGE, AUTHENTICATION_ERROR_TITLE, request);
    }

    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
    public ProblemDetail handleBadCredentialsException(final Exception ex, final HttpServletRequest request) {
        log.warn("Authentication failure at [{}]: {} - Message: {}",
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        return this.createProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password.", AUTHENTICATION_ERROR_TITLE, request);
    }

    @ExceptionHandler({ LockedException.class, DisabledException.class })
    public ProblemDetail handleLockedException(final AccountStatusException ex, final HttpServletRequest request) {
        log.warn("Login rejected due to account status [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        String message = ex.getMessage();
        String title = "Account Unavailable";
        if (ex instanceof  LockedException) {
            message = "Account is locked.";
            title = "Account Locked";
        } else if (ex instanceof DisabledException) {
            message = "Account is disabled.";
            title = "Account Disabled";
        }
        return this.createProblemDetail(HttpStatus.FORBIDDEN, message, title, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handlerAuthenticationException(final AuthenticationException ex, final HttpServletRequest request) {
        log.warn("Generic authentication error at URI [{}]: {}",  request.getRequestURI(), ex.getMessage());
        return this.createProblemDetail(HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR_MESSAGE, AUTHENTICATION_ERROR_TITLE, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handlerDataIntegrityViolationException(final DataIntegrityViolationException ex, final HttpServletRequest request) {
        log.warn("Data integrity violation at URI [{}]: {}",  request.getRequestURI(), ex.getMessage());
        String rootMessage = ex.getMostSpecificCause().getMessage();
        if (rootMessage != null && rootMessage.toLowerCase().contains("duplicate entry")) {
            return this.createProblemDetail(HttpStatus.CONFLICT, "Value already exists.", "Data Conflict", request);
        }
        return this.createProblemDetail(HttpStatus.BAD_REQUEST, "Please check your input.", "Integrity Violation", request);
    }

    // Helper
    private ProblemDetail createProblemDetail(final HttpStatus status, final String detail, final String title, final HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("urn:error:" + status.value()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
