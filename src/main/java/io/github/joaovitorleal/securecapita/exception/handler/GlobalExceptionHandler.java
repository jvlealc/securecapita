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
