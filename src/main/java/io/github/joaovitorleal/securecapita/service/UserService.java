package io.github.joaovitorleal.securecapita.service;

import io.github.joaovitorleal.securecapita.domain.AccountVerification;
import io.github.joaovitorleal.securecapita.domain.Role;
import io.github.joaovitorleal.securecapita.domain.TwoFactorVerification;
import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.domain.enums.VerificationType;
import io.github.joaovitorleal.securecapita.dto.UserRequestDto;
import io.github.joaovitorleal.securecapita.dto.UserResponseDto;
import io.github.joaovitorleal.securecapita.mapper.UserMapper;
import io.github.joaovitorleal.securecapita.exception.EmailAlreadyExistsException;
import io.github.joaovitorleal.securecapita.exception.RoleNotFoundByNameException;
import io.github.joaovitorleal.securecapita.exception.UserNotFoundByEmailException;
import io.github.joaovitorleal.securecapita.exception.UserNotFoundByIdException;
import io.github.joaovitorleal.securecapita.repository.AccountVerificationJpaRepository;
import io.github.joaovitorleal.securecapita.repository.RoleJpaRepository;
import io.github.joaovitorleal.securecapita.repository.TwoFactorVerificationJpaRepository;
import io.github.joaovitorleal.securecapita.repository.UserJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static io.github.joaovitorleal.securecapita.domain.enums.RoleType.ROLE_USER;

@Service
@Slf4j
public class UserService {

    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final AccountVerificationJpaRepository accountVerificationJpaRepository;
    private final TwoFactorVerificationJpaRepository  twoFactorVerificationJpaRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final NotificationService emailService;

    public UserService(
            UserJpaRepository userJpaRepository,
            RoleJpaRepository roleJpaRepository,
            AccountVerificationJpaRepository accountVerificationJpaRepository,
            TwoFactorVerificationJpaRepository twoFactorVerificationJpaRepository,
            UserMapper userMapper,
            PasswordEncoder encoder,
            NotificationService emailService
    ) {
        this.userJpaRepository = userJpaRepository;
        this.roleJpaRepository = roleJpaRepository;
        this.accountVerificationJpaRepository = accountVerificationJpaRepository;
        this.twoFactorVerificationJpaRepository = twoFactorVerificationJpaRepository;
        this.userMapper = userMapper;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        if(userJpaRepository.existsByEmail(userRequestDto.email())) {
            throw new EmailAlreadyExistsException("Email already exists. Please use a different email and try again.");
        }
        User user = userMapper.toEntity(userRequestDto);
        user.setPassword(encoder.encode(user.getPassword()));

        Role role = roleJpaRepository.findByName(ROLE_USER.name())
                .orElseThrow(() -> {
                    log.warn("No role found with name: {}", user.getRole().getName());
                    return new RoleNotFoundByNameException("No role found with name: " + user.getRole().getName());
                });
        user.setRole(role);

        User createdUser = userJpaRepository.save(user);

        String verificationUrl = this.getVerificationUrl(UUID.randomUUID().toString(), VerificationType.ACCOUNT.getType());
        AccountVerification accountVerification = AccountVerification.builder()
                .user(user)
                .url(verificationUrl)
                .build();
        accountVerificationJpaRepository.save(accountVerification);
        //emailService.sendMessage(user.getEmail(), verificationUrl);
        return userMapper.toResponseDto(createdUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        return userMapper.toResponseDto(
                userJpaRepository.findByEmail(email)
                        .orElseThrow(() -> new UserNotFoundByEmailException("No User found by email: " + email))
        );
    }

    @Transactional
    public void sendVerificationCode(UserResponseDto userResponseDto) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        String verificationCode = RandomStringUtils.secure().nextAlphanumeric(8).toUpperCase();
        twoFactorVerificationJpaRepository.deleteByUserId(userResponseDto.id());
        User existingUser = userJpaRepository.findById(userResponseDto.id())
                .orElseThrow(() -> new UserNotFoundByIdException("User not found with id: " + userResponseDto.id()));
        twoFactorVerificationJpaRepository.save(
                TwoFactorVerification.builder()
                        .user(existingUser)
                        .code(verificationCode)
                        .expirationDate(expirationDate)
                        .build()
        );
        log.info("2FA Code generated for user {}: {}", existingUser.getEmail(), verificationCode);

        emailService.sendMfaCode(existingUser.getFirstName(), existingUser.getEmail(), code);
        log.info("MFA Code sent to email: {}", existingUser.getEmail());
    }

    @Transactional
    public UserResponseDto verifyMfaCode(String email, String code) {
        User user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException("User not found with email: " + email));
        MfaVerification verification = twoFactorVerificationJpaRepository.findByUserId(user.getId())
                .orElseThrow(() -> new MfaVerificationNotFoundByUserIdException("MFA code not found."));
        if (!verification.getCode().equals(code)) {
            throw new MfaCodeInvalidException("Invalid MFA code.");
        }
        if (verification.getExpirationDate().isBefore(LocalDateTime.now())) {
            twoFactorVerificationJpaRepository.delete(verification);
            throw new MfaCodeExpiredException("MFA code expired.");
        }
        twoFactorVerificationJpaRepository.delete(verification);
        return userMapper.toResponseDto(user);
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/user/verify/" + type + "/" + key).toString();
    }
}
