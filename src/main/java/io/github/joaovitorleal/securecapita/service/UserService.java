package io.github.joaovitorleal.securecapita.service;

import io.github.joaovitorleal.securecapita.domain.AccountVerification;
import io.github.joaovitorleal.securecapita.domain.Role;
import io.github.joaovitorleal.securecapita.domain.MfaVerification;
import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.domain.enums.VerificationType;
import io.github.joaovitorleal.securecapita.dto.UserRequestDto;
import io.github.joaovitorleal.securecapita.dto.UserResponseDto;
import io.github.joaovitorleal.securecapita.exception.*;
import io.github.joaovitorleal.securecapita.mapper.UserMapper;
import io.github.joaovitorleal.securecapita.repository.AccountVerificationJpaRepository;
import io.github.joaovitorleal.securecapita.repository.RoleJpaRepository;
import io.github.joaovitorleal.securecapita.repository.MfaVerificationJpaRepository;
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
    private final MfaVerificationJpaRepository mfaVerificationJpaRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final NotificationService emailService;

    public UserService(
            UserJpaRepository userJpaRepository,
            RoleJpaRepository roleJpaRepository,
            AccountVerificationJpaRepository accountVerificationJpaRepository,
            MfaVerificationJpaRepository mfaVerificationJpaRepository,
            UserMapper userMapper,
            PasswordEncoder encoder,
            NotificationService emailService
    ) {
        this.userJpaRepository = userJpaRepository;
        this.roleJpaRepository = roleJpaRepository;
        this.accountVerificationJpaRepository = accountVerificationJpaRepository;
        this.mfaVerificationJpaRepository = mfaVerificationJpaRepository;
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
    public UserResponseDto getUserDtoByEmail(String email) {
        return userMapper.toResponseDto(
                userJpaRepository.findByEmail(email)
                        .orElseThrow(() -> new UserNotFoundByEmailException("No User found by email: " + email))
        );
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                        .orElseThrow(() -> new UserNotFoundByEmailException("No User found by email: " + email));
    }

    @Transactional
    public void sendMfaCode(UserResponseDto userResponseDto) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        String code = RandomStringUtils.secure().nextAlphanumeric(8).toUpperCase();
        mfaVerificationJpaRepository.deleteByUserId(userResponseDto.id());
        User existingUser = userJpaRepository.findById(userResponseDto.id())
                .orElseThrow(() -> new UserNotFoundByIdException("User not found with id: " + userResponseDto.id()));
        mfaVerificationJpaRepository.save(
                MfaVerification.builder()
                        .user(existingUser)
                        .code(code)
                        .expirationDate(expirationDate)
                        .build()
        );
        log.info("MFA Code generated for user {}: {}", existingUser.getEmail(), code);

        emailService.sendMfaCode(existingUser.getFirstName(), existingUser.getEmail(), code);
        log.info("MFA Code sent to email: {}", existingUser.getEmail());
    }

    @Transactional
    public UserResponseDto verifyMfaCode(String email, String code) {
        User user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException("User not found with email: " + email));
        MfaVerification mfaVerification = mfaVerificationJpaRepository.findByUserId(user.getId())
                .orElseThrow(() -> new MfaVerificationNotFoundByUserIdException("MFA code not found."));
        if (!mfaVerification.getCode().equals(code)) {
            throw new MfaCodeInvalidException("Invalid MFA code.");
        }
        if (mfaVerification.getExpirationDate().isBefore(LocalDateTime.now())) {
            mfaVerificationJpaRepository.delete(mfaVerification);
            throw new MfaCodeExpiredException("MFA code expired.");
        }
        mfaVerificationJpaRepository.delete(mfaVerification);
        return userMapper.toResponseDto(user);
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/user/verify/" + type + "/" + key).toString();
    }
}
