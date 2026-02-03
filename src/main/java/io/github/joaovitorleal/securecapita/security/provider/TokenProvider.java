package io.github.joaovitorleal.securecapita.security.provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.github.joaovitorleal.securecapita.exception.JwtAuthenticationInvalidException;
import io.github.joaovitorleal.securecapita.security.model.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

@Component
public class TokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenProvider.class);

    private static final String ISSUER = "JV_LEAL_DEV";
    private static final String AUDIENCE = "SECURECAPITA_API";
    private static final String AUTHORITIES = "authorities";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME_MILLIS = 1_800_000L; // 30 minutos
    private static final long REFRESH_TOKEN_EXPIRATION_TIME_MILLIS = 432_000_000L; // 5 dias

    private static final String MESSAGE_TOKEN_EXPIRED = "Your session has expired. Please log in again.";
    private static final String MESSAGE_TOKEN_INVALID = "Invalid security token.";
    private static final String MESSAGE_TOKEN_ERROR = "Authentication failed. Please try again.";

    @Value("${jwt.secret}")
    private String secret;

    public String createAccessToken(CustomUserDetails userPrincipal) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withIssuedAt(Date.from(Instant.now()))
                .withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, this.getClaimsFromUser(userPrincipal))
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME_MILLIS))
                .sign(Algorithm.HMAC512(secret.getBytes(StandardCharsets.UTF_8)));
    }

    public String createRefreshToken(CustomUserDetails userPrincipal) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withIssuedAt(Date.from(Instant.now()))
                .withSubject(userPrincipal.getUsername())
                .withExpiresAt(Date.from(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_TIME_MILLIS)))
                .sign(Algorithm.HMAC512(secret.getBytes(StandardCharsets.UTF_8)));
    }

    public String getSubject(String token, HttpServletRequest request) {
        try {
            return this.getJWTVerifier().verify(token).getSubject();
        } catch (TokenExpiredException e) {
            request.setAttribute("expiredMessage", e.getMessage());
            throw new JwtAuthenticationInvalidException(MESSAGE_TOKEN_EXPIRED, e);
        } catch (InvalidClaimException e) {
            request.setAttribute("invalidClaim", e.getMessage());
            throw new JwtAuthenticationInvalidException(MESSAGE_TOKEN_INVALID, e);
        } catch (Exception e) {
            LOGGER.error("Internal error verifying token subjects.", e);
            throw new JwtAuthenticationInvalidException(MESSAGE_TOKEN_ERROR, e);
        }
    }

    public List<GrantedAuthority> getAuthorities(String token) {
        return Arrays.stream(this.getClaimsFromToken(token))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Authentication getAuthentication(String email, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
        usernamePasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return  usernamePasswordAuthToken;
    }

    @Deprecated(forRemoval = true)
    public boolean isTokenValid(String email, String token) {
       try {
           if (StringUtils.isBlank(email)) {
               return false;
           }
           return  !this.isTokenExpired(this.getJWTVerifier(), token);
       } catch (JWTVerificationException e) {
           LOGGER.debug("[isTokenValid] Token validation failed: {}", e.getMessage());
           return false;
       }
    }

    private String[] getClaimsFromUser(CustomUserDetails userPrincipal) {
        return userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
    }

    private String[] getClaimsFromToken(String token) {
        try {
            return this.getJWTVerifier().verify(token)
                    .getClaim(AUTHORITIES)
                    .asArray(String.class);
        } catch (JWTVerificationException e) {
            LOGGER.debug("[getClaimsFromToken] Token validation failed: {}", e.getMessage(), e);
            throw new JwtAuthenticationInvalidException(MESSAGE_TOKEN_INVALID, e);
        }
    }

    private JWTVerifier getJWTVerifier() {
        return JWT.require(Algorithm.HMAC512(secret.getBytes(StandardCharsets.UTF_8)))
                .withIssuer(ISSUER)
                .build();
    }

    @Deprecated(forRemoval = true)
    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }
}
