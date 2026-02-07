package io.github.joaovitorleal.securecapita.service;

import io.github.joaovitorleal.securecapita.domain.*;
import io.github.joaovitorleal.securecapita.domain.enums.MfaType;
import io.github.joaovitorleal.securecapita.domain.enums.VerificationType;
import io.github.joaovitorleal.securecapita.dto.UserCreateRequestDto;
import io.github.joaovitorleal.securecapita.dto.UserResponseDto;
import io.github.joaovitorleal.securecapita.dto.UserUpdateRequestDto;
import io.github.joaovitorleal.securecapita.exception.*;
import io.github.joaovitorleal.securecapita.mapper.UserMapper;
import io.github.joaovitorleal.securecapita.repository.*;
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

    private final UserJpaRepository userRepository;
    private final RoleJpaRepository roleRepository;
    private final AccountVerificationJpaRepository accountVerificationRepository;
    private final MfaVerificationJpaRepository mfaVerificationRepository;
    private final ResetPasswordVerificationJpaRepository resetPasswordVerificationRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final NotificationService emailService;
    private final NotificationService smsService;

    public UserService(
            UserJpaRepository userRepository,
            RoleJpaRepository roleRepository,
            AccountVerificationJpaRepository accountVerificationRepository,
            MfaVerificationJpaRepository mfaVerificationRepository,
            ResetPasswordVerificationJpaRepository resetPasswordVerificationRepository,
            UserMapper userMapper,
            PasswordEncoder encoder,
            NotificationService emailService,
            NotificationService smsService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountVerificationRepository = accountVerificationRepository;
        this.mfaVerificationRepository = mfaVerificationRepository;
        this.resetPasswordVerificationRepository = resetPasswordVerificationRepository;
        this.userMapper = userMapper;
        this.encoder = encoder;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
        if(userRepository.existsByEmail(userCreateRequestDto.email())) {
            throw new EmailAlreadyExistsException(userCreateRequestDto.email());
        }
        String emailLower = userCreateRequestDto.email().toLowerCase();
        User user = userMapper.toEntity(userCreateRequestDto);
        user.setEmail(emailLower);
        user.setPassword(encoder.encode(user.getPassword()));

        Role role = roleRepository.findByName(ROLE_USER.name())
                .orElseThrow(() -> {
                    log.warn("No role found with name: {}", user.getRole().getName());
                    return new RoleNotFoundByNameException(user.getRole().getName());
                });
        user.setRole(role);

        User createdUser = userRepository.save(user);

        String verificationUrl = this.buildVerificationUrl(UUID.randomUUID().toString(), VerificationType.ACCOUNT.getType());
        AccountVerification accountVerification = new AccountVerification(user, verificationUrl);
        accountVerificationRepository.save(accountVerification);
        emailService.sendAccountVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl);
        return userMapper.toResponseDto(createdUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserDtoByEmail(String email) {
        return userMapper.toResponseDto(
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new UserNotFoundByEmailException(email))
        );
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                        .orElseThrow(() -> new UserNotFoundByEmailException(email));
    }

    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto userUpdateRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundByIdException(userId));

        if (userUpdateRequestDto.firstName() != null) user.setFirstName(userUpdateRequestDto.firstName());
        if (userUpdateRequestDto.lastName() != null) user.setLastName(userUpdateRequestDto.lastName());
        if (userUpdateRequestDto.phone() != null) user.setPhone(userUpdateRequestDto.phone());
        if (userUpdateRequestDto.title() != null) user.setTitle(userUpdateRequestDto.title());
        if (userUpdateRequestDto.bio() != null) user.setBio(userUpdateRequestDto.bio());

        if (userUpdateRequestDto.email() != null && !userUpdateRequestDto.email().isBlank()) {
            String newEmailLower = userUpdateRequestDto.email().toLowerCase();

            if (!newEmailLower.equalsIgnoreCase(user.getEmail())) {

                if (userRepository.existsByEmail(newEmailLower)) {
                    throw new EmailAlreadyExistsException(userUpdateRequestDto.email());
                }

                user.setEmail(newEmailLower);
                user.setEnabled(false);
                String verificationUrl = this.buildVerificationUrl(UUID.randomUUID().toString(), VerificationType.ACCOUNT.getType());
                accountVerificationRepository.deleteByUserId(user.getId());
                accountVerificationRepository.save(new AccountVerification(user, verificationUrl));
                emailService.sendAccountVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl);
            }
        }

        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUserById(Long userId) {
        if (userRepository.existsById(userId)){
            throw new UserNotFoundByIdException(userId);
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public void sendMfaCode(UserResponseDto userResponseDto) {
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        String code = RandomStringUtils.secure().nextAlphanumeric(8).toUpperCase();
        mfaVerificationRepository.deleteByUserId(userResponseDto.id());
        User existingUser = userRepository.findById(userResponseDto.id())
                .orElseThrow(() -> new UserNotFoundByIdException(userResponseDto.id()));
        mfaVerificationRepository.save(new MfaVerification(existingUser, code, expirationDate));
        if (userResponseDto.mfaType().equals(MfaType.SMS)) {
            smsService.sendMfaCode(existingUser.getFirstName(), existingUser.getPhone(), code);
            log.info("MFA code sent to phone: {}", existingUser.getPhone());
        } else {
            emailService.sendMfaCode(existingUser.getFirstName(), existingUser.getEmail(), code);
            log.info("MFA Code sent to email: {}", existingUser.getEmail());
        }
    }

    @Transactional
    public User verifyMfaCode(String email, String code) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundByEmailException(email));
            MfaVerification mfaVerification = mfaVerificationRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new MfaVerificationNotFoundByUserIdException("MFA code not found."));
            if (!mfaVerification.getCode().equals(code)) {
                throw new MfaCodeInvalidException("Invalid MFA code.");
            }
            if (mfaVerification.getExpirationDate().isBefore(LocalDateTime.now())) {
                mfaVerificationRepository.delete(mfaVerification);
                throw new MfaCodeExpiredException("MFA code expired.");
            }
            mfaVerificationRepository.delete(mfaVerification);
            return user;
        } catch (UserNotFoundByEmailException | MfaVerificationNotFoundByUserIdException ex) {
            log.warn("Error while verifying MFA code {}", ex.getMessage(), ex);
            throw new MfaCodeInvalidException("Invalid MFA code.");
        }
    }

    /**
     * Inicia o processo de redefinição de senha.
     *
     * @param email Email do usuário que solicitou a redefinição de senha.
     * @throws UserNotFoundByEmailException Se o usuário não for encontrado
     * */
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException(email));
        resetPasswordVerificationRepository.deleteByUserId(user.getId());
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(10).truncatedTo(ChronoUnit.SECONDS);
        String verificationUrl = buildVerificationUrl(UUID.randomUUID().toString(), VerificationType.PASSWORD.getType());
        resetPasswordVerificationRepository.save(new ResetPasswordVerification(user, verificationUrl, expirationDate));
        emailService.sendResetPasswordUrl(user.getFirstName(), user.getEmail(), verificationUrl);
    }


    /**
     * Valida a chave de redefinição de senha.
     *
     * @param key O token opaco (UUID) proveniente da URL de verificação
     * @return UserResponseDto Os dados públicos do usuário
     * @throws ResetPasswordVerificationInvalidException Se a chave não existir ou já foi consumida
     * @throws ResetPasswordVerificationExpiredException Se a chave existir mas está vencida
     * */
    @Transactional
    public UserResponseDto verifyResetPasswordKey(String key) {
        ResetPasswordVerification resetPasswordVerification = resetPasswordVerificationRepository.findByUrl(this.buildVerificationUrl(key, VerificationType.PASSWORD.getType()))
                .orElseThrow(() -> new ResetPasswordVerificationInvalidException("This reset link is invalid or has already been used."));
        if (resetPasswordVerification.getExpirationDate().isBefore(LocalDateTime.now())) {
            resetPasswordVerificationRepository.delete(resetPasswordVerification);
            throw new ResetPasswordVerificationExpiredException("The reset link has expired. Please request a new one.");
        }
        return userMapper.toResponseDto(resetPasswordVerification.getUser());
    }

    /**
     * Finaliza o processo de redefinição de senha, atualizando a senha do usuário e invalidando a chave de verificação.
     *
     * @param key UUID/chave de verificação para validar a permissão.
     * @param newPassword Nova senha bruta inserida pelo usuário.
     * @param confirmPassword Senha de confirmação para validação.
     * @throws PasswordMismatchException Se as senhas não coincidirem.
     * @throws ResetPasswordVerificationInvalidException Se a chave não existir ou já foi consumida
     * @throws ResetPasswordVerificationExpiredException Se a chave existir mas está vencido
     * */
    @Transactional
    public void resetPassword(String key, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match.");
        }
        ResetPasswordVerification resetPasswordVerification = resetPasswordVerificationRepository.findByUrl(this.buildVerificationUrl(key, VerificationType.PASSWORD.getType()))
                .orElseThrow(() -> new ResetPasswordVerificationInvalidException("This reset link is invalid or has already been used."));
        if (resetPasswordVerification.getExpirationDate().isBefore(LocalDateTime.now())) {
            resetPasswordVerificationRepository.delete(resetPasswordVerification);
            throw new ResetPasswordVerificationExpiredException("The reset link has expired. Please request a new one.");
        }
        User user = resetPasswordVerification.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        resetPasswordVerificationRepository.delete(resetPasswordVerification);
        emailService.sendResetPasswordConfirmationMessage(user.getFirstName(), user.getEmail());
    }

    /**
     * Realiza a ativação da conta.
     *
     * @param key UUID/chave presente na URL de verificação.
     * @return true se a conta acabou de ser habilitava ou false se já estava habilitada.
     * @throws AccountVerificationNotFoundByUrl Se a URL de verificação não for encontrada.
     * */
    @Transactional
    public boolean activateAccount(String key) {
        User user = accountVerificationRepository.findByUrl(this.buildVerificationUrl(key, VerificationType.ACCOUNT.getType()))
                .orElseThrow(() -> new AccountVerificationNotFoundByUrl("This link is not valid."))
                .getUser();
        if (!user.isEnabled()) {
            user.setEnabled(true);
            userRepository.save(user);
            emailService.sendAccountVerifiedMessage(user.getFirstName(), user.getEmail());
            return true;
        }
        return false;
    }

    private String buildVerificationUrl(String token, String type) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/users/verify/" + type + "/" + token).toUriString();
    }
}
