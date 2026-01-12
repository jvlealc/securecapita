package io.github.joaovitorleal.securecapita.resource;

import io.github.joaovitorleal.securecapita.dto.HttpResponse;
import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.dto.UserDto;
import io.github.joaovitorleal.securecapita.dto.UserRequestDto;
import io.github.joaovitorleal.securecapita.dto.UserResponseDto;
import io.github.joaovitorleal.securecapita.dto.form.LoginForm;
import io.github.joaovitorleal.securecapita.service.UserService;
import io.github.joaovitorleal.securecapita.utils.UriGenerator;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
        UserResponseDto userResponseDto = userService.getUserByEmail(loginForm.getEmail());
        return userResponseDto.usingMfa()
                ? this.sendVerificationCode(userResponseDto)
                : this.sendResponse(userResponseDto);
    }

    @PostMapping
    public ResponseEntity<HttpResponse> createUser(@RequestBody @Valid UserRequestDto userRequestDto) {
        UserResponseDto userResponseDto = userService.createUser(userRequestDto);
        return ResponseEntity.created(UriGenerator.generate(userResponseDto.id())).body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userResponseDto))
                        .message("User created")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    private ResponseEntity<HttpResponse> sendResponse(UserResponseDto userResponseDto) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userResponseDto))
                        .message("Login successful")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserResponseDto userResponseDto) {
        userService.sendVerificationCode(userResponseDto);
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userResponseDto))
                        .message("Verification code sent.")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build()
        );
    }
}


















