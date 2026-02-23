package com.landgo.controller;

import com.landgo.dto.request.ForgotPasswordRequest;
import com.landgo.dto.request.LoginRequest;
import com.landgo.dto.request.OAuth2Request;
import com.landgo.dto.request.RegisterRequest;
import com.landgo.dto.request.ResendVerificationRequest;
import com.landgo.dto.request.ResetPasswordRequest;
import com.landgo.dto.request.VerifyEmailRequest;
import com.landgo.dto.response.ApiResponse;
import com.landgo.dto.response.AuthResponse;
import com.landgo.dto.response.UserResponse;
import com.landgo.security.CurrentUser;
import com.landgo.security.UserPrincipal;
import com.landgo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs for user registration and login")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new seller or agent with email and password. A 6-digit verification code will be sent to the provided email.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. A verification code has been sent to your email.", response));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verify the user's email address using the 6-digit code sent during registration. Code expires in 15 minutes.")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification code", description = "Resend a new 6-digit verification code to the user's email. Invalidates any previous codes.")
    public ResponseEntity<ApiResponse<Void>> resendVerificationCode(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.success("Verification code has been resent to your email", null));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email", description = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/oauth2")
    @Operation(summary = "OAuth2 Login", description = "Login or register with Google or Apple")
    public ResponseEntity<ApiResponse<AuthResponse>> oauth2Login(@Valid @RequestBody OAuth2Request request) {
        AuthResponse response = authService.oauth2Login(request);
        return ResponseEntity.ok(ApiResponse.success("OAuth2 login successful", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get the currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        UserResponse response = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send a password reset link to the user's email address")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset link has been sent to your email", null));
    }

    @GetMapping("/reset-password/validate")
    @Operation(summary = "Validate reset token", description = "Check if a password reset token is valid and not expired")
    public ResponseEntity<ApiResponse<Void>> validateResetToken(@RequestParam String token) {
        authService.validateResetToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token is valid", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset the user's password using the token received via email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully. You can now login with your new password.", null));
    }
}
