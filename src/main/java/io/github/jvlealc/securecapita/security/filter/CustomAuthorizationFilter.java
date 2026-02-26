package io.github.jvlealc.securecapita.security.filter;

import io.github.jvlealc.securecapita.security.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final List<String> PUBLIC_ROUTES = List.of("/users/login", "/users/verify/code", "/users/refresh/token");

    private final TokenProvider tokenProvider;
    private final HandlerExceptionResolver resolver;

    public CustomAuthorizationFilter(
            TokenProvider tokenProvider,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.tokenProvider = tokenProvider;
        this.resolver = exceptionResolver;
    }

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = this.getToken(request);
            if (token != null && !token.isBlank()) {
                String email = tokenProvider.getSubject(token, request);
                if (email != null && !email.isBlank()) {
                    List<GrantedAuthority> authorities = tokenProvider.getAuthorities(token);
                    Authentication authentication = tokenProvider.getAuthentication(email, authorities, request);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error in authorization filter: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            resolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        // ignora méto-do CORS preflight
        if ("OPTIONS".equalsIgnoreCase(httpMethod)) {
            return true;
        }

        // ignora rotas públicas
        if (PUBLIC_ROUTES.contains(requestURI)) {
            return true;
        }

        // Ignora registro de usuários (POST /users)
        if ("/users".equals(requestURI) && httpMethod.equalsIgnoreCase("POST")) {
            return true;
        }

        // Ignora se não possui header Authorization
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        return authHeader == null || !authHeader.startsWith(TOKEN_PREFIX);
    }

    /**
     * Extrai o token JWT do header Authorization.
     *
     * @param request requisição corrente
     * @return Token JWT ou null se não existir
     **/
    private String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX) ) {
            return authHeader.substring(TOKEN_PREFIX.length()).trim();
        }
        return null;
    }
}
