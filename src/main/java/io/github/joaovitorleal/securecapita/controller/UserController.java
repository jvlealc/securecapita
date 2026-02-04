package io.github.joaovitorleal.securecapita.controller;

import io.github.joaovitorleal.securecapita.controller.utils.UriGenerator;
import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.dto.*;
import io.github.joaovitorleal.securecapita.dto.form.LoginFormDto;
import io.github.joaovitorleal.securecapita.mapper.UserMapper;
import io.github.joaovitorleal.securecapita.security.model.CustomUserDetails;
import io.github.joaovitorleal.securecapita.security.provider.TokenProvider;
import io.github.joaovitorleal.securecapita.service.UserService;
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

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto> login(@RequestBody @Valid LoginFormDto loginForm) {
        Authentication authentication = authenticationManager.authenticate(unauthenticated(loginForm.email(), loginForm.password()));
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        UserResponseDto userResponseDto = userMapper.toResponseDto(userPrincipal.getUser());
        return userResponseDto.usingMfa()
                ? this.sendVerificationCode(userResponseDto)
                : this.sendLoginSuccessResponse(userResponseDto, userPrincipal);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto> createUser(@RequestBody @Valid UserRequestDto userRequestDto) {
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

    @GetMapping("/verify/password/{token}")
    public ResponseEntity<ApiResponseDto> verifyResetPasswordToken(@PathVariable @NotBlank(message = "The token is required.") String token) {
        UserResponseDto userResponseDto = userService.verifyResetPasswordToken(token);
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

    @PostMapping("/password-resets/{token}")
    public ResponseEntity<ApiResponseDto> resetPasswordByKey(
            @PathVariable @NotBlank(message = "The token is required.") String token,
            @RequestBody @Valid ResetPasswordRequestDto resetPasswordRequestDto
    ) {
        userService.resetPassword(token, resetPasswordRequestDto.newPassword(), resetPasswordRequestDto.confirmPassword());
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

    private ResponseEntity<ApiResponseDto> sendVerificationCode(UserResponseDto userResponseDto) {
        userService.sendMfaCode(userResponseDto);
        return ResponseEntity.accepted()
                .body(
                    ApiResponseDto.builder()
                            .timestamp(LocalDateTime.now().toString())
                            .data(Map.of("email", userResponseDto.email(), "mfaRequired", true))
                            .message("Verification code sent.")
                            .status(HttpStatus.ACCEPTED)
                            .statusCode(HttpStatus.ACCEPTED.value())
                            .build()
                );
    }
}
