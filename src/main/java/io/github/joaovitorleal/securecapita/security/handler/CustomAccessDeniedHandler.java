package io.github.joaovitorleal.securecapita.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.joaovitorleal.securecapita.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    public static final String ACCESS_DENIED_MESSAGE = "You don't have enough permission.";

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String errorMessage = (accessDeniedException != null && accessDeniedException.getMessage() != null)
                ? accessDeniedException.getMessage()
                : ACCESS_DENIED_MESSAGE;

        ApiResponseDto httpResponse = ApiResponseDto.builder()
                .timestamp(Instant.now().toString())
                .reason(errorMessage)
                .status(HttpStatus.FORBIDDEN)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .build();

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, httpResponse);
        out.flush();
    }
}
