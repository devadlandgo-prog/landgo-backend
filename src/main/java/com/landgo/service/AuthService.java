package com.landgo.service;

import com.landgo.dto.request.ForgotPasswordRequest;
import com.landgo.dto.request.LoginRequest;
import com.landgo.dto.request.OAuth2Request;
import com.landgo.dto.request.RegisterRequest;
import com.landgo.dto.request.ResendVerificationRequest;
import com.landgo.dto.request.ResetPasswordRequest;
import com.landgo.dto.request.VerifyEmailRequest;
import com.landgo.dto.response.AuthResponse;
import com.landgo.dto.response.UserResponse;
import com.landgo.entity.EmailVerificationToken;
import com.landgo.entity.PasswordResetToken;
import com.landgo.entity.User;
import com.landgo.enums.AuthProvider;
import com.landgo.enums.Role;
import com.landgo.enums.UserType;
import com.landgo.exception.BadRequestException;
import com.landgo.exception.ResourceNotFoundException;
import com.landgo.factory.OAuth2StrategyFactory;
import com.landgo.mapper.UserMapper;
import com.landgo.repository.EmailVerificationTokenRepository;
import com.landgo.repository.PasswordResetTokenRepository;
import com.landgo.repository.UserRepository;
import com.landgo.security.JwtTokenProvider;
import com.landgo.security.UserPrincipal;
import com.landgo.strategy.OAuth2AuthenticationStrategy;
import com.landgo.strategy.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OAuth2StrategyFactory oAuth2StrategyFactory;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Validate agent-specific fields
        if (request.getUserType() == UserType.AGENT) {
            if (request.getAgencyName() == null || request.getAgencyName().isBlank()) {
                throw new BadRequestException("Agency name is required for agent registration");
            }
            if (request.getRecoLicenseNumber() == null || request.getRecoLicenseNumber().isBlank()) {
                throw new BadRequestException("RECO license number is required for agent registration");
            }
            if (request.getAgentAuthorizationAccepted() == null || !request.getAgentAuthorizationAccepted()) {
                throw new BadRequestException("Agent authorization must be accepted");
            }
        }

        // Split fullName into firstName/lastName for backward compatibility
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            String[] parts = request.getFullName().trim().split("\\s+", 2);
            request.setFirstName(parts[0]);
            request.setLastName(parts.length > 1 ? parts[1] : "");
        }

        User user = userMapper.toEntity(request);
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set role based on user type
        if (request.getUserType() == UserType.AGENT) {
            user.setRole(Role.AGENT);
            user.setUserType(UserType.AGENT);
            user.setAgentAuthorizationAccepted(true);
        } else {
            user.setRole(Role.SELLER);
            user.setUserType(UserType.SELLER);
        }

        user = userRepository.save(user);
        log.info("{} registered: {}", request.getUserType(), user.getEmail());

        // Generate and send email verification code
        generateAndSendVerificationCode(user);

        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("User logged in: {}", user.getEmail());
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse oauth2Login(OAuth2Request request) {
        OAuth2AuthenticationStrategy strategy = oAuth2StrategyFactory.getStrategy(request.getAuthProvider());
        OAuth2UserInfo userInfo = strategy.extractUserInfo(request.getToken());

        User user = userRepository.findByProviderIdAndAuthProvider(
                userInfo.getProviderId(), request.getAuthProvider())
                .orElseGet(() -> {
                    if (userRepository.existsByEmail(userInfo.getEmail())) {
                        throw new BadRequestException("Email already registered with different provider");
                    }
                    RegisterRequest registerRequest = strategy.toRegisterRequest(userInfo);
                    User newUser = userMapper.toEntity(registerRequest);
                    newUser.setRole(Role.SELLER);
                    newUser.setUserType(UserType.SELLER);
                    newUser.setAuthProvider(request.getAuthProvider());
                    newUser.setProviderId(userInfo.getProviderId());
                    newUser.setEmailVerified(true);
                    return userRepository.save(newUser);
                });

        log.info("OAuth2 login successful for: {}", user.getEmail());
        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    // ==========================================
    // EMAIL VERIFICATION
    // ==========================================

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        EmailVerificationToken token = emailVerificationTokenRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("No verification code found. Please request a new one."));

        if (token.isExpired()) {
            throw new BadRequestException("Verification code has expired. Please request a new one.");
        }

        if (token.getAttempts() >= MAX_VERIFICATION_ATTEMPTS) {
            throw new BadRequestException("Too many failed attempts. Please request a new verification code.");
        }

        if (!token.getCode().equals(request.getCode())) {
            token.incrementAttempts();
            emailVerificationTokenRepository.save(token);
            int remaining = MAX_VERIFICATION_ATTEMPTS - token.getAttempts();
            throw new BadRequestException("Invalid verification code. " + remaining + " attempt(s) remaining.");
        }

        // Code is correct — mark token as used and verify the user's email
        token.setUsed(true);
        emailVerificationTokenRepository.save(token);

        user.setEmailVerified(true);
        userRepository.save(user);

        // Invalidate any other active tokens
        emailVerificationTokenRepository.invalidateAllTokensForUser(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Transactional
    public void resendVerificationCode(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        generateAndSendVerificationCode(user);
        log.info("Verification code resent to: {}", user.getEmail());
    }

    private void generateAndSendVerificationCode(User user) {
        // Invalidate any existing verification tokens
        emailVerificationTokenRepository.invalidateAllTokensForUser(user);

        // Generate a 6-digit code
        String code = generateVerificationCode();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .code(code)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .build();
        emailVerificationTokenRepository.save(token);

        // Send the verification email asynchronously
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), code);
        log.info("Verification code generated and sent to: {}", user.getEmail());
    }

    private String generateVerificationCode() {
        int code = SECURE_RANDOM.nextInt(900000) + 100000; // 100000–999999
        return String.valueOf(code);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));

        // Only allow password reset for EMAIL auth provider
        if (user.getAuthProvider() != AuthProvider.EMAIL) {
            throw new BadRequestException(
                    "This account uses " + user.getAuthProvider().name() + " sign-in. Please use " +
                    user.getAuthProvider().name() + " to access your account.");
        }

        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.invalidateAllTokensForUser(user);

        // Generate a new reset token (UUID-based, 30 min expiry)
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Send the reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token);
        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public void validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset link"));

        if (resetToken.isExpired()) {
            throw new BadRequestException("Password reset link has expired. Please request a new one.");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset link"));

        if (resetToken.isExpired()) {
            throw new BadRequestException("Password reset link has expired. Please request a new one.");
        }

        // Update the user's password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidate all other tokens for this user
        passwordResetTokenRepository.invalidateAllTokensForUser(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }
}
