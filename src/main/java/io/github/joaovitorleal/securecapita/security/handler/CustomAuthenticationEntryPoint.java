package io.github.joaovitorleal.securecapita.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.joaovitorleal.securecapita.dto.ApiResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String UNAUTHORIZE_MESSAGE = "You need to log in to access this resource.";

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        String errorMessage = (authException != null && authException.getMessage() != null)
                ? authException.getMessage()
                : UNAUTHORIZE_MESSAGE;

        ApiResponseDto httpResponse = ApiResponseDto.builder()
                .timestamp(Instant.now().toString())
                .reason(errorMessage)
                .status(HttpStatus.UNAUTHORIZED)

                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build();

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, httpResponse);
        out.flush();
    }
}
