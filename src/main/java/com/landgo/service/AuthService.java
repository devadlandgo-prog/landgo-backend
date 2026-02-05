package com.landgo.service;

import com.landgo.dto.request.LoginRequest;
import com.landgo.dto.request.OAuth2Request;
import com.landgo.dto.request.RegisterRequest;
import com.landgo.dto.response.AuthResponse;
import com.landgo.dto.response.UserResponse;
import com.landgo.entity.User;
import com.landgo.enums.AuthProvider;
import com.landgo.enums.Role;
import com.landgo.exception.BadRequestException;
import com.landgo.exception.ResourceNotFoundException;
import com.landgo.factory.OAuth2StrategyFactory;
import com.landgo.mapper.UserMapper;
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = userMapper.toEntity(request);
        user.setRole(Role.USER);
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

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
                    newUser.setRole(Role.USER);
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
}
