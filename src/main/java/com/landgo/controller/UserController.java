package com.landgo.controller;

import com.landgo.dto.request.UpdateProfileRequest;
import com.landgo.dto.response.ApiResponse;
import com.landgo.dto.response.UserResponse;
import com.landgo.security.CurrentUser;
import com.landgo.security.UserPrincipal;
import com.landgo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management APIs")
public class UserController {

    private final AuthService authService;

    @GetMapping("/profile")
    @Operation(summary = "Get my profile",
               description = "Get the current user's full profile including active listings count, total views, and subscription info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@CurrentUser UserPrincipal userPrincipal) {
        UserResponse response = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update profile",
               description = "Update the current user's profile. All fields are optional — only provided fields will be updated. Professional bio is optional and can be set or cleared.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = authService.updateProfile(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
