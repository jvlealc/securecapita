package io.github.jvlealc.securecapita.controller;

import io.github.jvlealc.securecapita.controller.utils.UriGenerator;
import io.github.jvlealc.securecapita.domain.User;
import io.github.jvlealc.securecapita.dto.*;
import io.github.jvlealc.securecapita.dto.*;
import io.github.jvlealc.securecapita.dto.form.LoginFormDto;
import io.github.jvlealc.securecapita.exception.JwtAuthenticationInvalidException;
import io.github.jvlealc.securecapita.mapper.UserMapper;
import io.github.jvlealc.securecapita.security.model.CustomUserDetails;
import io.github.jvlealc.securecapita.security.provider.TokenProvider;
import io.github.jvlealc.securecapita.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @PostMapping
    public ResponseEntity<ApiResponseDto> createUser(@RequestBody @Valid UserCreateRequestDto userRequestDto) {
        UserResponseDto userResponseDto = userService.createUser(userRequestDto);
        return ResponseEntity.created(UriGenerator.generate(userResponseDto.id()))
                .body(
                        ApiResponseDto.builder()
                                .timestamp(LocalDateTime.now().toString())
                                .data(Map.of("user", userResponseDto))
                                .message("User created")
                                .status(HttpStatus.CREATED)
                                .statusCode(HttpStatus.CREATED.value())
                                .build()
                );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto> login(@RequestBody @Valid LoginFormDto loginForm) {
        Authentication authentication = authenticationManager.authenticate(unauthenticated(loginForm.email(), loginForm.password()));
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        UserResponseDto userResponseDto = userMapper.toResponseDto(userPrincipal.getUser());
        return userResponseDto.usingMfa()
                ? this.sendVerificationMfaCode(userResponseDto)
                : this.sendLoginSuccessResponse(userResponseDto, userPrincipal);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDto> getUserProfile(Authentication authentication) {
        UserResponseDto userResponseDto = userService.getUserDtoByEmail(authentication.getName());
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userResponseDto))
                        .message("Profile retrieved")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/verify/code")
    public ResponseEntity<ApiResponseDto> verifyMfaCode(@RequestBody @Valid MfaVerificationRequestDto mfaVerificationRequestDto) {
        User user = userService.verifyMfaCode(mfaVerificationRequestDto.email(), mfaVerificationRequestDto.code());
        CustomUserDetails userPrincipal = new CustomUserDetails(user);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of(
                                "user", userMapper.toResponseDto(user),
                                "access_token", tokenProvider.createAccessToken(userPrincipal),
                                "refresh_token", tokenProvider.createRefreshToken(userPrincipal)
                        ))
                        .message("Login successful")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // INÍCIO - Sistema de redefinição de senha para usuário não autenticado.

    @PostMapping("/password-resets/{email}")
    public ResponseEntity<ApiResponseDto> resetPassword(@PathVariable @Email(message = "Invalid email.") String email) {
        userService.resetPassword(email);
        return ResponseEntity.accepted().body(
                ApiResponseDto.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .message("Email sent. Please check your email to reset your password.")
                        .status(HttpStatus.ACCEPTED)
                        .statusCode(HttpStatus.ACCEPTED.value())
                        .build()
        );
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<ApiResponseDto> verifyResetPasswordKey(@PathVariable @NotBlank(message = "The key is required.") String key) {
        UserResponseDto userResponseDto = userService.verifyResetPasswordKey(key);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userResponseDto))
                        .message("Please enter a new password.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/password-resets/{key}")
    public ResponseEntity<ApiResponseDto> resetPasswordByKey(
            @PathVariable @NotBlank(message = "The key is required.") String key,
            @RequestBody @Valid ResetPasswordRequestDto resetPasswordRequestDto
    ) {
        userService.resetPassword(key, resetPasswordRequestDto.newPassword(), resetPasswordRequestDto.confirmPassword());
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .message("Password reset successfully.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    // FIM - Sistema de redefinição de senha para usuário não autenticado.

    @GetMapping("/verify/account/{token}")
    public ResponseEntity<ApiResponseDto> verifyAccountByToken(@PathVariable @NotBlank(message = "The token is required.") String token) {
        boolean wasActivated = userService.activateAccount(token);
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .message(wasActivated ? "Account verified" : "Account already verified")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/refresh/token")
    public ResponseEntity<ApiResponseDto> refreshToken(HttpServletRequest request) {
            String authHeader = request.getHeader(HEADER_AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
                throw new JwtAuthenticationInvalidException("Refresh token missing or invalid.");
            }

            String token = authHeader.substring(TOKEN_PREFIX.length());
            User user =  userService.getUserByEmail(tokenProvider.getSubject(token, request));
            CustomUserDetails userPrincipal = new CustomUserDetails(user);

            return ResponseEntity.ok(
                    ApiResponseDto.builder()
                            .timestamp(LocalDateTime.now().toString())
                            .data(Map.of(
                                    "user", userMapper.toResponseDto(user),
                                    "access_token", tokenProvider.createAccessToken(userPrincipal),
                                    "refresh_token", tokenProvider.createRefreshToken(userPrincipal)
                            ))
                            .message("Token refresh")
                            .status(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build()
            );
    }

    private ResponseEntity<ApiResponseDto> sendLoginSuccessResponse(UserResponseDto userResponseDto, CustomUserDetails userPrincipal) {
        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of(
                                "user", userResponseDto,
                                "access_token", tokenProvider.createAccessToken(userPrincipal),
                                "refresh_token", tokenProvider.createRefreshToken(userPrincipal)
                        ))
                        .message("Login successful")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    private ResponseEntity<ApiResponseDto> sendVerificationMfaCode(UserResponseDto userResponseDto) {
        userService.sendMfaCode(userResponseDto);

        String mfaTarget = switch (userResponseDto.mfaType()) {
            case SMS -> userResponseDto.phone();
            case EMAIL -> userResponseDto.email();
        };

        Map<String, Object> responseData = Map.of(
                "mfaRequired", true,
                "mfaTarget", mfaTarget,
                "mfaType", userResponseDto.mfaType().name()
        );

        return ResponseEntity.accepted()
                .body(
                        ApiResponseDto.builder()
                                .timestamp(LocalDateTime.now().toString())
                                .data(responseData)
                                .message("Verification code sent.")
                                .status(HttpStatus.ACCEPTED)
                                .statusCode(HttpStatus.ACCEPTED.value())
                                .build()
                );
    }
}
